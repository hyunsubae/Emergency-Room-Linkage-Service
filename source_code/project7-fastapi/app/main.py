from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routes import emergency
import os
from dotenv import load_dotenv

# .env 파일 로드
load_dotenv()

# 환경변수 확인
if not os.getenv("OPENAI_KEY"):
    raise ValueError("OPENAI_KEY environment variable is not set")

app = FastAPI()

# CORS 미들웨어 설정
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# OpenAI API 키 설정
os.environ["OPENAI_API_KEY"] = os.getenv("OPENAI_KEY")

# 모든 라우터 포함
app.include_router(emergency.router)

@app.get("/")
async def root():
    return {"message": "Hello World"} 