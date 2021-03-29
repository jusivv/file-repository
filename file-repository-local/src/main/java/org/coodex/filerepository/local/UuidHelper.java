package org.coodex.filerepository.local;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;

import java.time.Instant;
import java.util.UUID;

public class UuidHelper {
    private static final long NUM_HUNDRED_NANOS_IN_A_SECOND = 10_000_000L;

    private static final long NUM_HUNDRED_NANOS_FROM_UUID_EPOCH_TO_UNIX_EPOCH = 122_192_928_000_000_000L;

    private static NoArgGenerator generator = Generators.timeBasedGenerator();


    /**
     * Extracts the Instant (with the maximum available 100ns precision) from the given time-based (version 1) UUID.
     *
     * @return the {@link Instant} extracted from the given time-based UUID
     * @throws UnsupportedOperationException If this UUID is not a version 1 UUID
     */
    public static Instant getInstantFromUUID(final UUID uuid) {
        final long hundredNanosSinceUnixEpoch = uuid.timestamp() - NUM_HUNDRED_NANOS_FROM_UUID_EPOCH_TO_UNIX_EPOCH;
        final long secondsSinceUnixEpoch = hundredNanosSinceUnixEpoch / NUM_HUNDRED_NANOS_IN_A_SECOND;
        final long nanoAdjustment = ((hundredNanosSinceUnixEpoch % NUM_HUNDRED_NANOS_IN_A_SECOND) * 100);
        return Instant.ofEpochSecond(secondsSinceUnixEpoch, nanoAdjustment);
    }

    public static String getUUIDString() {
        UUID uuid = generator.generate();
        return uuid.toString().replaceAll("-", "");
    }
}
