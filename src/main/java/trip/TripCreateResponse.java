package trip;

import java.util.UUID;

public record TripCreateResponse(UUID tripId, String message) {
}
