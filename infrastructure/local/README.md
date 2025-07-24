# MarketPulse Local Infrastructure

## 사용법

### 1. Redis 시작
```bash
cd infrastructure/local
docker-compose up -d redis
```

### 2. Redis 연결 확인
```bash
docker exec -it marketpulse-redis redis-cli ping
# 응답: PONG
```

### 3. Redis 중지
```bash
docker-compose down
```

### 4. 모든 데이터 삭제 (주의!)
```bash
docker-compose down -v
```

## 서비스 포트

- **Redis**: `localhost:6379`

## 유용한 명령어

```bash
# 로그 확인
docker-compose logs -f redis

# Redis CLI 접속
docker exec -it marketpulse-redis redis-cli

# 컨테이너 상태 확인
docker-compose ps
```
