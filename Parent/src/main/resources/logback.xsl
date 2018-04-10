<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <statusListener class="ch.qos.logback.core.status.NopStatusListener"/>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <!-- By default, encoders are assigned the type ch.qos.logback.classic.encoder.PatternLayoutEncoder -->
        <encoder class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
            <pattern>%d{HH:mm:ss.SSS} [%thread] %highlight(%-3level) %cyan(%logger) - %msg %n</pattern>
        </encoder>
        <!--<layout class="ch.qos.logback.classic.PatternLayout">-->
        <!--<Pattern>%d{HH:mm:ss.SSS} [%thread] %highlight(%-3level) %cyan(%logger) - %msg %n</Pattern>-->
        <!--</layout>-->
    </appender>

    <logger name="org.springframework" level="warn"/>
    <logger name="org.hibernate" level="warn"/>
    <logger name="com.reinpharma" level="debug"/>

    <root>
        <appender-ref ref="CONSOLE"/>
    </root>
</configuration>
