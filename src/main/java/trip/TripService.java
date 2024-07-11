package trip;

import com.rocketseat.planner.exception.InvalidDateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TripService {
    private final TripRepository tripRepository;

    @Autowired
    public TripService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    public Trip createTrip(TripRequestPayload tripRequestPayload) {
        // Validação das datas
        if (tripRequestPayload.startDate().isAfter(tripRequestPayload.endDate())) {
            throw new InvalidDateException("A data de começo da viagem deve ser inferior à data de término.");
        }

        Trip trip = new Trip();
        trip.setOwnerName(tripRequestPayload.owner_name());
        trip.setStartAt(tripRequestPayload.startDate().atStartOfDay());
        trip.setEndsAt(tripRequestPayload.endDate().atStartOfDay());

        return tripRepository.save(trip);
    }

}
