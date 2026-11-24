package com.connectprojects.backend.attendance;

import com.connectprojects.backend.account.AppUser;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class AttendanceRecordTest {
    @Test
    void allowsMultipleBreaksButOnlyOneActiveBreakAndRequiresBreakToEndBeforeCheckout() {
        AttendanceRecord record = new AttendanceRecord(
                mock(AppUser.class), LocalDate.of(2026, 11, 24), LocalTime.of(9, 0));

        record.startBreak("MEAL", LocalTime.of(12, 0));

        assertThrows(IllegalStateException.class,
                () -> record.startBreak("REST", LocalTime.of(12, 5)));
        assertThrows(IllegalStateException.class,
                () -> record.checkOut(LocalTime.of(17, 0)));

        record.endBreak(LocalTime.of(12, 30));
        record.startBreak("REST", LocalTime.of(15, 0));
        record.endBreak(LocalTime.of(15, 10));
        record.checkOut(LocalTime.of(17, 0));

        assertEquals(2, record.getBreaks().size());
        assertEquals(30, record.getBreaks().get(0).getDurationMinutes());
        assertEquals(10, record.getBreaks().get(1).getDurationMinutes());
        assertEquals(LocalTime.of(17, 0), record.getCheckOut());
    }

    @Test
    void rejectsBreaksBeforeCheckInAndEndingBreakWithoutOneInProgress() {
        AttendanceRecord record = new AttendanceRecord(
                mock(AppUser.class), LocalDate.of(2026, 11, 24), LocalTime.of(9, 0));

        assertThrows(IllegalStateException.class,
                () -> record.startBreak("GENERAL", LocalTime.of(8, 59)));
        assertThrows(IllegalStateException.class,
                () -> record.endBreak(LocalTime.of(12, 0)));
    }
}
