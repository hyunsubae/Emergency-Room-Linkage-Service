from typing import Dict, Tuple
import torch
import requests
import json
from openai import OpenAI
import pandas as pd
from transformers import AutoTokenizer, AutoModelForSequenceClassification

class EmergencyService:
    _instance = None
    _is_initialized = False

    def __new__(cls):
        if cls._instance is None:
            cls._instance = super(EmergencyService, cls).__new__(cls)
        return cls._instance

    def __init__(self):
        if not self._is_initialized:
            # 모델과 데이터 로드
            save_directory = "fine_tuned_bert"
            self.model = AutoModelForSequenceClassification.from_pretrained(save_directory)
            self.tokenizer = AutoTokenizer.from_pretrained(save_directory)
            self.emergency_df = pd.read_csv("emergency_data.csv")
            self.fire_station_df = pd.read_csv("fire_station_data.csv", encoding='euc-kr')
            self.client = OpenAI()
            self._is_initialized = True
        
    async def process_emergency(self, text: str, latitude: float, longitude: float, client_id: str, client_key: str) -> Dict:
        # 텍스트 요약
        summary_result = await self._text_summary(text)
        
        # 긴급도 예측
        urgency_level, probabilities = self._predict(text)
        
        # 가까운 병원 찾기
        hospitals = self._recommend_hospital(latitude, longitude, client_id, client_key)
        
        # 가까운 소방서 찾기
        fire_stations = self._find_nearest_fire_station(latitude, longitude, client_id, client_key)
        
        return {
            "summary": summary_result,
            "urgency_level": int(urgency_level),
            "hospitals": hospitals.to_dict('records') if hospitals is not None else [],
            "fire_stations": fire_stations.to_dict('records') if fire_stations is not None else []
        }

    async def _text_summary(self, input_text: str) -> str:
        system_role = '''당신은 응급상황에 대한 텍스트에서 핵심 내용을 훌륭하게 요약해주는 어시스턴트입니다.
        응답은 다음의 형식을 지켜주세요.
        {"summary": \"텍스트 요약\",
        "keyword" : \"핵심 키워드(3가지)\"}
        '''
        
        response = self.client.chat.completions.create(
            model="gpt-3.5-turbo",
            messages=[
                {"role": "system", "content": system_role},
                {"role": "user", "content": input_text}
            ]
        )
        
        answer = response.choices[0].message.content
        parsed_answer = json.loads(answer)
        return parsed_answer["summary"] + ', ' + parsed_answer["keyword"]

    def _predict(self, text: str) -> Tuple[int, torch.Tensor]:
        inputs = self.tokenizer(text, return_tensors="pt", truncation=True, padding=True)
        inputs = {key: value for key, value in inputs.items()}

        with torch.no_grad():
            outputs = self.model(**inputs)

        logits = outputs.logits
        probabilities = logits.softmax(dim=1)
        pred = torch.argmax(probabilities, dim=-1).item()

        return pred, probabilities

    def _get_distance(self, start_lat: float, start_lng: float, dest_lat: float, dest_lng: float, c_id: str, c_key: str) -> Tuple[float, float]:
        url = "https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving"
        headers = {
            "X-NCP-APIGW-API-KEY-ID": c_id,
            "X-NCP-APIGW-API-KEY": c_key,
        }
        params = {
            "start": f"{start_lng},{start_lat}",
            "goal": f"{dest_lng},{dest_lat}",
            "option": "trafast"
        }

        response = requests.get(url, headers=headers, params=params)
        if response.status_code == 200:
            result = response.json()
            summary = result['route']['trafast'][0]['summary']
            return summary['distance'] / 1000, summary['duration'] / (1000 * 60)  # 거리(km), 소요시간(분)
        
        return None, None

    def _recommend_hospital(self, start_lat: float, start_lng: float, c_id: str, c_key: str):
        temp = self.emergency_df.loc[
            self.emergency_df['위도'].between(start_lat-0.05, start_lat+0.05) & 
            self.emergency_df['경도'].between(start_lng-0.05, start_lng+0.05)
        ].copy()

        # 거리와 소요시간 계산
        temp[['거리', '소요시간']] = temp.apply(
            lambda x: pd.Series(
                self._get_distance(start_lat, start_lng, x['위도'], x['경도'], c_id, c_key)
            ), 
            axis=1
        )
        
        # 10분 이내 도착 가능한 병원 필터링
        result = temp[temp['소요시간'] <= 10].sort_values(by='소요시간')
        
        # 10분 이내 병원이 없으면 가장 가까운 병원 1개 반환
        if result.empty:
            return temp.nsmallest(1, '소요시간')
        return result

    def _find_nearest_fire_station(self, lat: float, lon: float, c_id: str, c_key: str):
        try:
            temp = self.fire_station_df.loc[
                (self.fire_station_df['X좌표'].between(lat-0.05, lat+0.05)) & 
                (self.fire_station_df['Y좌표'].between(lon-0.05, lon+0.05))
            ].copy()

            if temp.empty:
                print(f"No fire stations found for coordinates: lat={lat}, lon={lon}")
                return None

            temp['X좌표'] = temp['X좌표'].astype(float)
            temp['Y좌표'] = temp['Y좌표'].astype(float)

            # 거리와 소요시간 계산
            temp[['도로 거리', '소요시간']] = temp.apply(
                lambda x: pd.Series(
                    self._get_distance(
                        start_lat=lat,
                        start_lng=lon,
                        dest_lat=x['X좌표'],
                        dest_lng=x['Y좌표'],
                        c_id=c_id,
                        c_key=c_key
                    )
                ),
                axis=1
            )

            temp = temp.dropna(subset=['도로 거리', '소요시간'])
            
            if temp.empty:
                print("All fire stations were filtered out due to invalid distances")
                return None

            # 10분 이내 도착 가능한 소방서 필터링
            result = temp[temp['소요시간'] <= 10].sort_values(by='소요시간')
            
            # 10분 이내 소방서가 없으면 가장 가까운 소방서 1개 반환
            if result.empty:
                return temp.nsmallest(1, '소요시간')
            return result

        except Exception as e:
            print(f"Error in _find_nearest_fire_station: {str(e)}")
            return None
