package me.rgunny.marketpulse.alert.domain;

public enum AlertStatus {

    PENDING,   // 생성됨 (미발송)
    SENT,      // 전송 요청 큐/하위계층까지 전달됨
    SUCCEEDED, // 최종 성공 (notification에서 성공 이벤트 수신)
    FAILED,    // 최종 실패 (notification에서 실패 이벤트 수신)
    CANCELED,  // 취소
    THROTTLED, // 쿨다운 등으로 억제
    EXPIRED    // 만료
}
