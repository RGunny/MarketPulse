package me.rgunny.marketpulse.event.marketdata.infrastructure.dto.kis;

public record KISTokenResponse(
        String access_token,
        String token_type,
        int expires_in
) {
    public String getAccessToken() {
        return access_token;
    }
}