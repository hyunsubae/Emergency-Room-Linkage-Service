# 응급 시스템 FAST API

## 시작하기


1. conda activate <your-conda-env> 로 가상환경 들어가기 `cmd`환경인지 반드시 확인할 것.
2. root 폴더에 `.env` 파일 생성
```
  OPENAI_KEY= "YOUR_OPENAI_KEY"
  C_ID= "YOUR_C_ID"
  C_KEY= "YOUR_C_KEY"
```
   
3. root 폴더에 `fine_tuned_bert` 가 존재하여야 함
4. pip install -r requirement.txt
5. `uvicorn app.main:app --reload` `cmd`에 작성

```
INFO:     Will watch for changes in these directories: 
INFO:     Uvicorn running on http://127.0.0.1:8000 (Press CTRL+C to quit)
INFO:     Started reloader process [34100] using WatchFiles
INFO:     Started server process [34156]
INFO:     Waiting for application startup.
INFO:     Application startup complete.
```

문구가 뜬다면 실행 완료.
