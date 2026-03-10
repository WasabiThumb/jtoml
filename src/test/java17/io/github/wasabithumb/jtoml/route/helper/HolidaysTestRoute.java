/*
 * Copyright 2025 Xavier Pedraza
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.wasabithumb.jtoml.route.helper;

import io.github.wasabithumb.jtoml.route.TestRoute;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public abstract class HolidaysTestRoute implements TestRoute {

    protected void validateDocument(
            Collection<? extends Month> months,
            Map<? extends String, ? extends Collection<? extends Holiday>> holidays
    ) {
        validateMonths(months);
        for (Month month : months) {
            Collection<? extends Holiday> h = holidays.get(month.name());
            if (h == null) continue;
            validateHolidays(h);
        }
    }

    protected void validateMonths(Collection<? extends Month> months) {
        int f = 0;
        for (Month month : months) {
            int m = 1 << month.ordinal();
            assertEquals(0, f & m);
            f |= m;
        }
        assertEquals(Month.COUNT, months.size());
        assertEquals(Month.COUNT, Integer.numberOfTrailingZeros(~f));
    }

    protected void validateHolidays(Collection<? extends Holiday> holidays) {
        Iterator<? extends Holiday> iter = holidays.iterator();
        assertTrue(iter.hasNext());

        Holiday last = iter.next();
        Holiday next;

        while (iter.hasNext()) {
            next = iter.next();
            assertTrue(next.compareTo(last) > 0);
            last = next;
        }
    }

    //

    protected enum Month {
        JAN,
        FEB,
        MAR,
        APR,
        MAY,
        JUN,
        JUL,
        AUG,
        SEP,
        OCT,
        NOV,
        DEC;

        static final int COUNT = values().length;
    }

    protected record Holiday(
            String name,
            LocalDate date
    ) implements Comparable<Holiday> {

        @Override
        public int compareTo(Holiday other) {
            int cmp;
            cmp = Integer.compare(this.date.getMonthValue(), other.date.getMonthValue());
            if (cmp != 0) return cmp;
            cmp = Integer.compare(this.date.getDayOfMonth(), other.date.getDayOfMonth());
            return cmp;
        }

    }

}
