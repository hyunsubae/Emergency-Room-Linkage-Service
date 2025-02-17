import os
from dotenv import load_dotenv

load_dotenv()

OPENAI_KEY = os.getenv("OPENAI_KEY")
C_ID = os.getenv("C_ID")
C_KEY = os.getenv("C_KEY")