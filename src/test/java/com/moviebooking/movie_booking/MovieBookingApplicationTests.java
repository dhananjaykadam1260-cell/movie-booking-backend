package com.moviebooking.movie_booking;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Needs a running MySQL and the environment variables from README.md.
 * Excluded from the default build (see surefire config in pom.xml).
 */
@Tag("integration")
@SpringBootTest
class MovieBookingApplicationTests {

	@Test
	void contextLoads() {
	}

}
