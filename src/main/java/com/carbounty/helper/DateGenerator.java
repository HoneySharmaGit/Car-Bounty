package com.carbounty.helper;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

@Component
public class DateGenerator {

	public String dateAndTimeGenerator() {
		ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Kolkata"));
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, EEE, hh:mm:ss z");
		String formattedDate = formatter.format(zonedDateTime);
		return formattedDate;
	}

}
