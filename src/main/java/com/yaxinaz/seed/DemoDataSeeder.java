package com.yaxinaz.seed;

import com.yaxinaz.community.Community;
import com.yaxinaz.community.CommunityMembership;
import com.yaxinaz.community.CommunityMembershipRepository;
import com.yaxinaz.community.CommunityRepository;
import com.yaxinaz.community.CommunityType;
import com.yaxinaz.community.MembershipStatus;
import com.yaxinaz.event.AttendanceStatus;
import com.yaxinaz.event.CommunityEvent;
import com.yaxinaz.event.CommunityEventRepository;
import com.yaxinaz.event.EventAttendance;
import com.yaxinaz.event.EventAttendanceRepository;
import com.yaxinaz.feed.Post;
import com.yaxinaz.feed.PostRepository;
import com.yaxinaz.feed.PostType;
import com.yaxinaz.issue.Issue;
import com.yaxinaz.issue.IssueActivity;
import com.yaxinaz.issue.IssueActivityRepository;
import com.yaxinaz.issue.IssueActivityType;
import com.yaxinaz.issue.IssueCategory;
import com.yaxinaz.issue.IssuePriority;
import com.yaxinaz.issue.IssueRepository;
import com.yaxinaz.issue.IssueStatus;
import com.yaxinaz.issue.IssueSupport;
import com.yaxinaz.issue.IssueSupportRepository;
import com.yaxinaz.lostfound.LostFoundItem;
import com.yaxinaz.lostfound.LostFoundRepository;
import com.yaxinaz.lostfound.LostFoundStatus;
import com.yaxinaz.lostfound.LostFoundType;
import com.yaxinaz.poll.Poll;
import com.yaxinaz.poll.PollOption;
import com.yaxinaz.poll.PollOptionRepository;
import com.yaxinaz.poll.PollRepository;
import com.yaxinaz.poll.PollVote;
import com.yaxinaz.poll.PollVoteRepository;
import com.yaxinaz.provider.ProviderProfile;
import com.yaxinaz.provider.ProviderProfileRepository;
import com.yaxinaz.provider.ServiceCategory;
import com.yaxinaz.review.Review;
import com.yaxinaz.review.ReviewRepository;
import com.yaxinaz.servicerequest.ServiceRequest;
import com.yaxinaz.servicerequest.ServiceRequestRepository;
import com.yaxinaz.servicerequest.ServiceRequestStatus;
import com.yaxinaz.user.Role;
import com.yaxinaz.user.User;
import com.yaxinaz.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

/**
 * Realistic demo data for the "Explore Demo" flow (spec sections 123-131). Dev-profile only -
 * never runs against a production database. Idempotent: if the primary demo resident account
 * already exists, seeding is skipped entirely so restarts don't duplicate data.
 * <p>
 * Deliberately does NOT fake the "284 Residents" figure from spec section 123 - every displayed
 * number in this app (member counts, statistics, ratings) is computed live from real rows, and
 * section 131's Mock Policy explicitly forbids mocking community membership. A modest, honest
 * number of seeded residents is used instead; the spec's "284" was illustrative flavor text, not a
 * literal requirement to fabricate a number the rest of the system would then contradict.
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);
    private static final String DEMO_PASSWORD = "Demo1234!";
    /** Extra quick-login accounts for manual local poking around - short password only works here
     * because seeding saves the User entity directly and never goes through RegisterRequest's
     * {@code @Size(min = 8)} validation. Dev-profile only, never touches a production database. */
    private static final String QUICK_TEST_PASSWORD = "1234";

    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final CommunityMembershipRepository membershipRepository;
    private final IssueRepository issueRepository;
    private final IssueActivityRepository issueActivityRepository;
    private final IssueSupportRepository issueSupportRepository;
    private final PostRepository postRepository;
    private final PollRepository pollRepository;
    private final PollOptionRepository pollOptionRepository;
    private final PollVoteRepository pollVoteRepository;
    private final CommunityEventRepository eventRepository;
    private final EventAttendanceRepository attendanceRepository;
    private final LostFoundRepository lostFoundRepository;
    private final ProviderProfileRepository providerProfileRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.existsByEmailAndDeletedFalse("resident@yaxinaz.az")) {
            log.info("Demo data already present - skipping seeding.");
            return;
        }
        log.info("Seeding demo data for Yaxin.az (dev profile)...");

        User platformAdmin = user("Aygün", "Məmmədova", "platform@yaxinaz.az", Role.PLATFORM_ADMIN);
        User communityAdmin = user("Aysun", "Farhadli", "admin@yaxinaz.az", Role.COMMUNITY_ADMIN);
        User resident = user("Elvin", "Quliyev", "resident@yaxinaz.az", Role.RESIDENT);
        User providerUser = user("Rashad", "Aliyev", "provider@yaxinaz.az", Role.SERVICE_PROVIDER);

        List<User> extraResidents = List.of(
                user("Leyla", "Hasanova", "leyla.h@yaxinaz.az", Role.RESIDENT),
                user("Tural", "Mammadli", "tural.m@yaxinaz.az", Role.RESIDENT),
                user("Nigar", "Aliyeva", "nigar.a@yaxinaz.az", Role.RESIDENT),
                user("Orkhan", "Huseynov", "orkhan.h@yaxinaz.az", Role.RESIDENT),
                user("Sabina", "Rzayeva", "sabina.r@yaxinaz.az", Role.RESIDENT),
                user("Kamran", "Ismayilov", "kamran.i@yaxinaz.az", Role.RESIDENT)
        );

        Community community = communityRepository.save(Community.builder()
                .name("Green Park Residence")
                .description("A modern residential complex in Baku with 6 buildings and shared green spaces.")
                .type(CommunityType.RESIDENTIAL_COMPLEX)
                .city("Baku")
                .district("Yasamal")
                .address("28 May Street 45")
                .createdBy(communityAdmin.getId())
                .build());

        approve(communityAdmin, community);
        approve(resident, community);
        extraResidents.forEach(r -> approve(r, community));

        seedIssues(community, resident, communityAdmin, extraResidents);
        seedPosts(community, communityAdmin, resident);
        seedPoll(community, communityAdmin, resident, extraResidents);
        seedEvents(community, communityAdmin, resident, extraResidents);
        seedLostFound(community, resident, extraResidents);
        seedProvidersAndServices(providerUser, resident);

        User quickAdmin = userWithPassword("Test", "Admin", "admin@test.com", Role.PLATFORM_ADMIN, QUICK_TEST_PASSWORD);
        User quickUser = userWithPassword("Test", "User", "user@test.com", Role.RESIDENT, QUICK_TEST_PASSWORD);
        approve(quickUser, community);

        log.info("Demo data seeding complete. Demo accounts (password '{}'): platform@yaxinaz.az, "
                + "admin@yaxinaz.az, resident@yaxinaz.az, provider@yaxinaz.az", DEMO_PASSWORD);
        log.info("Quick test accounts (password '{}'): {} (PLATFORM_ADMIN), {} (RESIDENT)",
                QUICK_TEST_PASSWORD, quickAdmin.getEmail(), quickUser.getEmail());
    }

    private User user(String firstName, String lastName, String email, Role role) {
        return userWithPassword(firstName, lastName, email, role, DEMO_PASSWORD);
    }

    private User userWithPassword(String firstName, String lastName, String email, Role role, String rawPassword) {
        return userRepository.save(User.builder()
                .firstName(firstName).lastName(lastName).email(email)
                .password(passwordEncoder.encode(rawPassword))
                .role(role).preferredLanguage("EN").enabled(true).build());
    }

    private void approve(User user, Community community) {
        membershipRepository.save(CommunityMembership.builder()
                .user(user).community(community).status(MembershipStatus.APPROVED)
                .joinedAt(Instant.now().minus(30, ChronoUnit.DAYS))
                .reviewedAt(Instant.now().minus(30, ChronoUnit.DAYS))
                .reviewedBy(community.getCreatedBy())
                .build());
    }

    private void seedIssues(Community community, User resident, User admin, List<User> extras) {
        Issue elevator = issueRepository.save(Issue.builder()
                .community(community).createdBy(resident.getId())
                .title("Elevator unavailable - Building B")
                .description("The elevator in Building B has stopped working again and several elderly residents cannot easily use the stairs.")
                .aiSummary("The Building B elevator is out of service, creating accessibility difficulties for elderly residents.")
                .category(IssueCategory.ELEVATOR).priority(IssuePriority.HIGH).status(IssueStatus.IN_PROGRESS)
                .buildingOrLocation("Building B").build());
        activity(elevator, IssueActivityType.CREATED, "Issue reported by resident", resident.getId());
        activity(elevator, IssueActivityType.AI_ANALYZED, "AI analyzed the report", null);
        activity(elevator, IssueActivityType.STATUS_CHANGED, "Status changed from OPEN to ACKNOWLEDGED", admin.getId());
        activity(elevator, IssueActivityType.STATUS_CHANGED, "Status changed from ACKNOWLEDGED to IN_PROGRESS", admin.getId());
        extras.forEach(r -> issueSupportRepository.save(IssueSupport.builder().issue(elevator).user(r).build()));

        Issue parking = issueRepository.save(Issue.builder()
                .community(community).createdBy(extras.get(0).getId())
                .title("Parking gate malfunction")
                .description("The main parking gate does not open automatically for residents with valid access cards.")
                .category(IssueCategory.PARKING).priority(IssuePriority.MEDIUM).status(IssueStatus.ACKNOWLEDGED)
                .buildingOrLocation("Main entrance").build());
        activity(parking, IssueActivityType.CREATED, "Issue reported by resident", extras.get(0).getId());

        Issue water = issueRepository.save(Issue.builder()
                .community(community).createdBy(extras.get(1).getId())
                .title("Low water pressure - Building A")
                .description("Water pressure on the top floors of Building A has been very low for two days.")
                .category(IssueCategory.WATER).priority(IssuePriority.HIGH).status(IssueStatus.OPEN)
                .buildingOrLocation("Building A").build());
        activity(water, IssueActivityType.CREATED, "Issue reported by resident", extras.get(1).getId());

        Issue lighting = issueRepository.save(Issue.builder()
                .community(community).createdBy(extras.get(2).getId())
                .title("Hallway lighting problem")
                .description("Several hallway lights on floor 3 have been flickering and occasionally go dark at night.")
                .category(IssueCategory.LIGHTING).priority(IssuePriority.LOW).status(IssueStatus.RESOLVED)
                .buildingOrLocation("Building C, Floor 3").resolvedAt(Instant.now().minus(2, ChronoUnit.DAYS)).build());
        activity(lighting, IssueActivityType.CREATED, "Issue reported by resident", extras.get(2).getId());
        activity(lighting, IssueActivityType.RESOLVED, "Issue marked resolved", admin.getId());

        Issue noise = issueRepository.save(Issue.builder()
                .community(community).createdBy(extras.get(3).getId())
                .title("Late-night noise complaint")
                .description("Loud music from an apartment in Building D has continued past midnight several nights this week.")
                .category(IssueCategory.NOISE).priority(IssuePriority.MEDIUM).status(IssueStatus.OPEN)
                .buildingOrLocation("Building D").build());
        activity(noise, IssueActivityType.CREATED, "Issue reported by resident", extras.get(3).getId());

        Issue waste = issueRepository.save(Issue.builder()
                .community(community).createdBy(extras.get(4).getId())
                .title("Waste collection delay")
                .description("Garbage bins near Building A have not been collected in over a week.")
                .category(IssueCategory.WASTE).priority(IssuePriority.MEDIUM).status(IssueStatus.WAITING_FOR_VENDOR)
                .buildingOrLocation("Building A").build());
        activity(waste, IssueActivityType.CREATED, "Issue reported by resident", extras.get(4).getId());
        activity(waste, IssueActivityType.STATUS_CHANGED, "Status changed from ACKNOWLEDGED to WAITING_FOR_VENDOR", admin.getId());

        Issue camera = issueRepository.save(Issue.builder()
                .community(community).createdBy(extras.get(5).getId())
                .title("Security camera offline")
                .description("The security camera covering the rear parking area appears to be offline since yesterday.")
                .category(IssueCategory.SECURITY).priority(IssuePriority.CRITICAL).status(IssueStatus.OPEN)
                .buildingOrLocation("Rear parking area").build());
        activity(camera, IssueActivityType.CREATED, "Issue reported by resident", extras.get(5).getId());
    }

    private void activity(Issue issue, IssueActivityType type, String message, Long actorId) {
        issueActivityRepository.save(IssueActivity.builder().issue(issue).type(type).message(message).actorUserId(actorId).build());
    }

    private void seedPosts(Community community, User admin, User resident) {
        postRepository.save(Post.builder().community(community).authorId(admin.getId())
                .postType(PostType.ANNOUNCEMENT).title("Scheduled Water Maintenance")
                .content("Water service will be temporarily unavailable tomorrow between 10:00 and 15:00 due to scheduled maintenance.")
                .pinned(true).build());
        postRepository.save(Post.builder().community(community).authorId(admin.getId())
                .postType(PostType.EVENT).title("Weekend Cleanup Drive")
                .content("Join your neighbors this Saturday for a community cleanup around the green spaces. Gloves and bags provided.")
                .build());
        postRepository.save(Post.builder().community(community).authorId(admin.getId())
                .postType(PostType.ALERT).title("Security Reminder")
                .content("Please ensure entrance doors close fully behind you and do not let unfamiliar visitors follow you in.")
                .build());
        postRepository.save(Post.builder().community(community).authorId(resident.getId())
                .postType(PostType.QUESTION).title(null)
                .content("Does anyone have a recommendation for a reliable electrician? Need an outlet replaced.")
                .build());
        postRepository.save(Post.builder().community(community).authorId(resident.getId())
                .postType(PostType.GENERAL).title(null)
                .content("Would anyone else be interested in an EV charging station near Building A? Curious how many of us drive electric.")
                .build());
        postRepository.save(Post.builder().community(community).authorId(admin.getId())
                .postType(PostType.MARKETPLACE).title("Kids Activity Day Sign-ups Open")
                .content("Sign-ups for this month's Kids Activity Day are now open - see the Events tab for details.")
                .build());
    }

    private void seedPoll(Community community, User admin, User resident, List<User> extras) {
        Poll poll = pollRepository.save(Poll.builder().community(community)
                .question("Should EV charging stations be installed near Building A?")
                .createdBy(admin.getId())
                .expiresAt(Instant.now().plus(14, ChronoUnit.DAYS))
                .active(true).build());
        PollOption yes = pollOptionRepository.save(PollOption.builder().poll(poll).optionText("Yes").build());
        PollOption no = pollOptionRepository.save(PollOption.builder().poll(poll).optionText("No").build());
        PollOption more = pollOptionRepository.save(PollOption.builder().poll(poll).optionText("Need more information").build());

        pollVoteRepository.save(PollVote.builder().poll(poll).option(yes).user(resident).build());
        pollVoteRepository.save(PollVote.builder().poll(poll).option(yes).user(extras.get(0)).build());
        pollVoteRepository.save(PollVote.builder().poll(poll).option(yes).user(extras.get(1)).build());
        pollVoteRepository.save(PollVote.builder().poll(poll).option(no).user(extras.get(2)).build());
        pollVoteRepository.save(PollVote.builder().poll(poll).option(more).user(extras.get(3)).build());
    }

    private void seedEvents(Community community, User admin, User resident, List<User> extras) {
        CommunityEvent meeting = eventRepository.save(CommunityEvent.builder().community(community)
                .title("Community Meeting").description("Monthly resident meeting to discuss ongoing maintenance and budget items.")
                .location("Community Hall").startTime(Instant.now().plus(5, ChronoUnit.DAYS))
                .endTime(Instant.now().plus(5, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS))
                .createdBy(admin.getId()).build());
        attend(meeting, resident, AttendanceStatus.GOING);
        attend(meeting, extras.get(0), AttendanceStatus.GOING);
        attend(meeting, extras.get(1), AttendanceStatus.MAYBE);

        CommunityEvent cleanup = eventRepository.save(CommunityEvent.builder().community(community)
                .title("Weekend Cleanup").description("Community cleanup around the green spaces and parking areas.")
                .location("Main Courtyard").startTime(Instant.now().plus(3, ChronoUnit.DAYS))
                .endTime(Instant.now().plus(3, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS))
                .createdBy(admin.getId()).build());
        attend(cleanup, extras.get(2), AttendanceStatus.GOING);

        CommunityEvent kids = eventRepository.save(CommunityEvent.builder().community(community)
                .title("Kids Activity Day").description("Games and activities for children of all ages.")
                .location("Playground").startTime(Instant.now().plus(10, ChronoUnit.DAYS))
                .endTime(Instant.now().plus(10, ChronoUnit.DAYS).plus(3, ChronoUnit.HOURS))
                .capacity(30).createdBy(admin.getId()).build());
        attend(kids, resident, AttendanceStatus.MAYBE);

        eventRepository.save(CommunityEvent.builder().community(community)
                .title("Resident Safety Meeting").description("Overview of building security procedures and emergency contacts.")
                .location("Community Hall").startTime(Instant.now().minus(20, ChronoUnit.DAYS))
                .endTime(Instant.now().minus(20, ChronoUnit.DAYS).plus(1, ChronoUnit.HOURS))
                .createdBy(admin.getId()).build());
    }

    private void attend(CommunityEvent event, User user, AttendanceStatus status) {
        attendanceRepository.save(EventAttendance.builder().event(event).user(user).status(status).build());
    }

    private void seedLostFound(Community community, User resident, List<User> extras) {
        lostFoundRepository.save(LostFoundItem.builder().community(community).createdBy(extras.get(0).getId())
                .type(LostFoundType.FOUND).title("Keys Found Near Building B")
                .description("A set of keys with a blue keychain found near the Building B entrance.")
                .location("Building B entrance").status(LostFoundStatus.ACTIVE)
                .imageUrl("/images/lostfound/keys.jpg").build());
        lostFoundRepository.save(LostFoundItem.builder().community(community).createdBy(resident.getId())
                .type(LostFoundType.LOST).title("Lost Cat")
                .description("Orange tabby cat, answers to Milo, last seen near the playground.")
                .location("Playground area").status(LostFoundStatus.ACTIVE)
                .imageUrl("/images/lostfound/cat.jpg").build());
        lostFoundRepository.save(LostFoundItem.builder().community(community).createdBy(extras.get(1).getId())
                .type(LostFoundType.FOUND).title("Found Wallet")
                .description("Brown leather wallet found in the main lobby.")
                .location("Main lobby").status(LostFoundStatus.CLAIMED)
                .imageUrl("/images/lostfound/wallet.jpg").build());
        lostFoundRepository.save(LostFoundItem.builder().community(community).createdBy(extras.get(2).getId())
                .type(LostFoundType.LOST).title("Lost AirPods")
                .description("White AirPods case, possibly dropped near the parking garage.")
                .location("Parking garage").status(LostFoundStatus.ACTIVE)
                .imageUrl("/images/lostfound/airpods.jpg").build());
    }

    private void seedProvidersAndServices(User providerUser, User resident) {
        ProviderProfile fixPro = providerProfileRepository.save(ProviderProfile.builder()
                .user(providerUser).businessName("FixPro Plumbing")
                .bio("Licensed plumbers serving Baku residential communities for over 10 years.")
                .serviceArea("Baku").verified(true)
                .categories(Set.of(ServiceCategory.PLUMBING, ServiceCategory.HANDYMAN))
                .averageRating(0).reviewCount(0).build());

        User voltUser = user("Kamal", "Huseynli", "volt.electric@yaxinaz.az", Role.SERVICE_PROVIDER);
        providerProfileRepository.save(ProviderProfile.builder().user(voltUser).businessName("Volt Electric")
                .bio("Residential and commercial electrical services.").serviceArea("Baku").verified(true)
                .categories(Set.of(ServiceCategory.ELECTRICAL)).averageRating(0).reviewCount(0).build());

        User cleanHomeUser = user("Gunel", "Sadigova", "cleanhome@yaxinaz.az", Role.SERVICE_PROVIDER);
        providerProfileRepository.save(ProviderProfile.builder().user(cleanHomeUser).businessName("CleanHome")
                .bio("Professional home and apartment cleaning services.").serviceArea("Baku").verified(true)
                .categories(Set.of(ServiceCategory.CLEANING)).averageRating(0).reviewCount(0).build());

        User smartFixUser = user("Elnur", "Bagirov", "smartfix@yaxinaz.az", Role.SERVICE_PROVIDER);
        providerProfileRepository.save(ProviderProfile.builder().user(smartFixUser).businessName("SmartFix Appliances")
                .bio("Appliance repair specialists - washing machines, refrigerators, and more.").serviceArea("Baku")
                .verified(false).categories(Set.of(ServiceCategory.APPLIANCE_REPAIR)).averageRating(0).reviewCount(0).build());

        User cityMoveUser = user("Farid", "Nabiyev", "citymove@yaxinaz.az", Role.SERVICE_PROVIDER);
        providerProfileRepository.save(ProviderProfile.builder().user(cityMoveUser).businessName("CityMove")
                .bio("Local moving and furniture transport services.").serviceArea("Baku")
                .verified(true).categories(Set.of(ServiceCategory.MOVING)).averageRating(0).reviewCount(0).build());

        User petCareUser = user("Aynur", "Guliyeva", "petcare.baku@yaxinaz.az", Role.SERVICE_PROVIDER);
        providerProfileRepository.save(ProviderProfile.builder().user(petCareUser).businessName("PetCare Baku")
                .bio("Dog walking, pet sitting, and grooming services.").serviceArea("Baku")
                .verified(false).categories(Set.of(ServiceCategory.PET_CARE)).averageRating(0).reviewCount(0).build());

        ServiceRequest completedRequest = serviceRequestRepository.save(ServiceRequest.builder()
                .customer(resident).provider(fixPro).category(ServiceCategory.PLUMBING)
                .description("Kitchen sink was leaking and needed a new seal.")
                .status(ServiceRequestStatus.COMPLETED).completedAt(Instant.now().minus(5, ChronoUnit.DAYS)).build());
        Review review = reviewRepository.save(Review.builder().serviceRequest(completedRequest).author(resident)
                .provider(fixPro).rating(5).comment("Quick and professional service, highly recommend!").build());
        fixPro.setReviewCount(1);
        fixPro.setAverageRating(review.getRating());
        providerProfileRepository.save(fixPro);

        serviceRequestRepository.save(ServiceRequest.builder()
                .customer(resident).provider(fixPro).category(ServiceCategory.PLUMBING)
                .description("Bathroom faucet is dripping constantly.")
                .status(ServiceRequestStatus.REQUESTED).build());
    }
}
