#!/bin/bash

# Docker Compose 실행 스크립트
# 실행 위치: 프로젝트 루트에서

cd "$(dirname "$0")/../.." || exit

echo "🚀 Starting MarketPulse Infrastructure..."

# Docker Compose 실행
docker-compose up -d

# 서비스 상태 확인
echo "⏳ Waiting for services to be ready..."
sleep 5

echo "✅ Services status:"
docker-compose ps

echo "📊 Service URLs:"
echo "  - MongoDB: mongodb://localhost:27017"
echo "  - Redis: redis://localhost:6379"

echo "🎉 MarketPulse Infrastructure is ready!"
