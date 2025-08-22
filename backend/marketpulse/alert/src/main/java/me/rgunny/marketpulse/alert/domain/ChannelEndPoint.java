package me.rgunny.marketpulse.alert.domain;

public record ChannelEndPoint(
        ChannelType type,
        Provider provider,
        String destination // slackChannelId, discordWebhookUrl, email, etc..
)
{}
