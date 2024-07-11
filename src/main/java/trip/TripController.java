package trip;

import com.rocketseat.planner.activity.ActivityData;
import com.rocketseat.planner.activity.ActivityRequestPayload;
import com.rocketseat.planner.activity.ActivityResponse;
import com.rocketseat.planner.activity.ActivityService;
import com.rocketseat.planner.link.LinkData;
import com.rocketseat.planner.link.LinkRequestPayload;
import com.rocketseat.planner.link.LinkResponse;
import com.rocketseat.planner.link.LinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import participant.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/trips")
public class TripController {

    private final TripService tripService;

    @Autowired
    private LinkService linkService;
    @Autowired
    private ParticipantService participantService;
    @Autowired
    private ActivityService activityService;
    @Autowired
    private TripRepository repository;


    @Autowired
    public TripController(TripService tripService) {
        this.tripService = tripService;
    }
    @PostMapping
    public ResponseEntity<TripCreateResponse> createTrip(@RequestBody TripRequestPayload payLoad){
        Trip newTrip = new Trip(payLoad);

        Trip trip = tripService.createTrip(payLoad);

        TripCreateResponse response = new TripCreateResponse(trip.getId(), "Viagem criada com sucesso!");

        this.repository.save(newTrip);

        this.participantService.registerParticipantsToEvent(payLoad.emails_to_invite(), newTrip);

        return new ResponseEntity<>(response, HttpStatus.CREATED);

    }
    @GetMapping("/{id}")
    public ResponseEntity<Trip> getTripDatails(@PathVariable UUID id){
        Optional<Trip> trip = this.repository.findById(id);

        return trip.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Trip> updateTrip(@PathVariable UUID id, @RequestBody TripRequestPayload payLoad){
        Optional<Trip> trip = this.repository.findById(id);

        if(trip.isPresent()){
            Trip rawTrip = trip.get();
            rawTrip.setEndsAt(LocalDateTime.parse(payLoad.ends_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setStartAt(LocalDateTime.parse(payLoad.starts_at(), DateTimeFormatter.ISO_DATE_TIME));
            rawTrip.setDestination(payLoad.destination());

            this.repository.save(rawTrip);

            return ResponseEntity.ok(rawTrip);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}/confirm")
    public ResponseEntity<Trip> confirmTrip(@PathVariable UUID id){
        Optional<Trip> trip = this.repository.findById(id);

        if(trip.isPresent()){
            Trip rawTrip = trip.get();
            rawTrip.setIsConfirmed(true);
            this.repository.save(rawTrip);
            this.participantService.triggerConfirmationEmailToParticipants(id);
            return ResponseEntity.ok(rawTrip);
        }

        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<ParticipantCreateResponse> inviteParticipant(@PathVariable UUID id, @RequestBody ParticipantRequestPayload payLoad){
        Optional<Trip> trip = this.repository.findById(id);

        if(trip.isPresent()){
            Trip rawTrip = trip.get();

            ParticipantCreateResponse participantResponse = this.participantService.registerParticipantToEvent(payLoad.email(), rawTrip);

            if(rawTrip.getIsConfirmed()) this.participantService.triggerConfirmationEmailToParticipant(payLoad.email());

            return ResponseEntity.ok(participantResponse);
        }

        return ResponseEntity.notFound().build();

    }

    @GetMapping("/{id}/activities")
    public ResponseEntity<List<ActivityData>> getAllActivities(@PathVariable UUID id){
        List<ActivityData> activityDataList = this.activityService.getAllActivitiesFromId(id);

        return ResponseEntity.ok(activityDataList);
    }

    @PostMapping("/{id}/activities")
    public ResponseEntity<ActivityResponse> registerActivity(@PathVariable UUID id, @RequestBody ActivityRequestPayload payLoad){
        Optional<Trip> trip = this.repository.findById(id);

        if(trip.isPresent()){
            Trip rawTrip = trip.get();

            ActivityResponse activityResponse = this.activityService.registerActivity(payLoad, rawTrip);



            return ResponseEntity.ok(activityResponse);
        }

        return ResponseEntity.notFound().build();

    }
    
    @GetMapping("/{id}/participants")
    public ResponseEntity<List<ParticipantData>> getAllParticipants(@PathVariable UUID id){
        List<ParticipantData> participantList = this.participantService.getAllParticipantsFromEvent(id);

        return ResponseEntity.ok(participantList);
    }

    @PostMapping("/{id}/links")
    public ResponseEntity<LinkResponse> registerLink(@PathVariable UUID id, @RequestBody LinkRequestPayload payLoad){
        Optional<Trip> trip = this.repository.findById(id);

        if(trip.isPresent()){
            Trip rawTrip = trip.get();

            LinkResponse linkResponse = this.linkService.registerLink(payLoad, rawTrip);

            return ResponseEntity.ok(linkResponse);
        }

        return ResponseEntity.notFound().build();

    }

    @GetMapping("/{id}/links")
    public ResponseEntity<List<LinkData>> getAllLinks(@PathVariable UUID id){
        List<LinkData> linkDataList = this.linkService.getAllLinksFromTrip(id);

        return ResponseEntity.ok(linkDataList);
    }


}
