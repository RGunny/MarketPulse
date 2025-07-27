// MongoDB 초기화 스크립트
// 이 파일은 MongoDB 컨테이너가 처음 시작될 때 실행됩니다.

// marketpulse 데이터베이스로 전환
db = db.getSiblingDB('marketpulse');

// 사용자 생성
db.createUser({
  user: 'marketpulse_user',
  pwd: 'marketpulse_pass',
  roles: [
    {
      role: 'readWrite',
      db: 'marketpulse'
    }
  ]
});

// 컬렉션 생성
db.createCollection('stock_prices');
db.createCollection('watchlist');
db.createCollection('price_events');

// 인덱스 생성
db.stock_prices.createIndex({ symbol: 1, timestamp: -1 });
db.stock_prices.createIndex({ timestamp: 1 }, { expireAfterSeconds: 2592000 }); // 30일 후 자동 삭제
db.watchlist.createIndex({ symbol: 1 }, { unique: true });
db.price_events.createIndex({ symbol: 1, timestamp: -1 });

// 초기 감시 종목 데이터 삽입
db.watchlist.insertMany([
  {
    symbol: '005930',
    name: '삼성전자',
    category: 'CORE',
    priority: 1,
    interval: 30,
    active: true,
    createdAt: new Date()
  },
  {
    symbol: '000660',
    name: 'SK하이닉스',
    category: 'CORE',
    priority: 1,
    interval: 30,
    active: true,
    createdAt: new Date()
  },
  {
    symbol: '373220',
    name: 'LG에너지솔루션',
    category: 'CORE',
    priority: 1,
    interval: 30,
    active: true,
    createdAt: new Date()
  }
]);

print('MarketPulse MongoDB initialization completed!');
