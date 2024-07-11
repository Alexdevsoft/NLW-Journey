package trip;

import java.time.LocalDate;
import java.util.List;

public record TripRequestPayload(LocalDate startDate, LocalDate endDate, String destination, String starts_at, String ends_at, List<String> emails_to_invite, String owner_email, String owner_name) {

}
