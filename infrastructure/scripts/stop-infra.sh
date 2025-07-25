#!/bin/bash

# Docker Compose 종료 스크립트
# 실행 위치: 프로젝트 루트에서

cd "$(dirname "$0")/../.." || exit

echo "🛑 Stopping MarketPulse Infrastructure..."

# Docker Compose 종료
docker-compose down

echo "✅ All services stopped!"
