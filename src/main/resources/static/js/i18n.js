/**
 * Dictionary-based i18n (spec section 122). Covers UI chrome across every page - buttons, labels,
 * headings, placeholders, empty states, toasts. Backend enums (roles, statuses, categories) and all
 * user-authored content (issue text, post content, names, bios) intentionally stay as-is - only the
 * app's own interface copy is translated.
 */
const i18n = (() => {
  const dict = {
    EN: {
      // Nav / shell
      dashboard: 'Dashboard', community: 'Community', issues: 'Issues', feed: 'Feed',
      polls: 'Polls', events: 'Events', lostFound: 'Lost & Found', services: 'Services',
      notifications: 'Notifications', profile: 'Profile', admin: 'Admin', search: 'Search',
      reportProblem: 'Report a Problem', logout: 'Log out', signIn: 'Sign in', signUp: 'Sign up',
      welcomeBack: 'Welcome back', joinYourCommunity: 'Join your community',
      email: 'Email', password: 'Password', firstName: 'First name', lastName: 'Last name',
      loading: 'Loading...', save: 'Save', cancel: 'Cancel', delete: 'Delete', confirm: 'Confirm',
      noResults: 'Nothing to show here yet.', apply: 'Apply', close: 'Close',

      // Shared across pages
      viewAll: 'View all', noLocationSpecified: 'No location specified',
      justNow: 'just now', minutesAgo: '{n}m ago', hoursAgo: '{n}h ago', daysAgo: '{n}d ago',
      joinCommunityFirst: 'Join a community first.', noCommunitySelected: 'No community selected',
      goToDashboard: 'Go to Dashboard', prevPage: 'Previous', nextPage: 'Next',
      pageOf: 'Page {current} of {total}', post: 'Post', titleLabel: 'Title',
      categoryLabel: 'Category', priorityLabel: 'Priority', descriptionLabel: 'Description',
      questionLabel: 'Question', typeLabel: 'Type', contentLabel: 'Content',
      noCommentsYetTitle: 'No comments yet', beFirstToComment: 'Be the first to comment.',
      writeCommentPlaceholder: 'Write a comment...', addCommentPlaceholder: 'Add a comment...',

      // Dashboard
      greetingMorning: 'morning', greetingAfternoon: 'afternoon', greetingEvening: 'evening',
      greetingGood: 'Good', loadingYourCommunity: 'Loading your community...',
      notJoinedCommunity: 'You have not joined a community yet.',
      joinCommunityTitle: 'Join a community to get started',
      joinCommunityMessage: 'Browse and join your residence to report issues, see the feed, and more.',
      openIssues: 'Open Issues', resolvedThisWeek: 'Resolved This Week',
      upcomingEvents: 'Upcoming Events', communityMembers: 'Community Members',
      aiCommunitySummary: 'AI Community Summary', recentIssues: 'Recent Issues',
      communityFeed: 'Community Feed', recommendedServices: 'Recommended Services',
      noIssuesYetTitle: 'No issues yet', noIssuesYetMessage: 'Nothing has been reported in this community yet.',
      noPostsYetTitle: 'No posts yet', noPostsYetDashboardMessage: 'Be the first to post in your community feed.',
      noEventsYetTitle: 'No upcoming events', noEventsYetMessage: 'Check back soon for community events.',
      noProvidersYetTitle: 'No providers yet', noProvidersYetMessage: 'Verified providers will appear here.',
      unableToLoadStats: 'Unable to load statistics.',
      alertNeedsAttention: '{critical} critical and {escalated} escalated issue(s) need attention.',
      reviewNow: 'Review now', aiUnavailableShort: 'AI assistance is temporarily unavailable.',
      unableToLoadDashboard: 'Unable to load dashboard: ',

      // Issues list + Smart Issue Reporting
      issuesTitle: 'Issues', issuesSubtitle: 'Everything reported in your community.',
      staleOnly: 'Stale only', escalatedOnly: 'Escalated only',
      joinCommunityFromDashboard: 'Join a community from your dashboard first.',
      noIssuesFoundTitle: 'No issues found', noIssuesFoundMessage: 'Nothing matches these filters right now.',
      unableToLoadIssues: 'Unable to load issues: ',
      whatHappened: 'What happened?', describeProblemPlaceholder: 'Describe the problem in your own words...',
      buildingLocationOptional: 'Building / location (optional)', buildingLocationPlaceholder: 'e.g. Building B',
      analyzeAndReport: 'Analyze & Report', pleaseDescribeMore: 'Please describe the problem in a bit more detail.',
      aiLoadingUnderstanding: 'Understanding your report...', aiLoadingCategory: 'Identifying the issue category...',
      aiLoadingUrgency: 'Checking urgency...', aiLoadingSimilar: 'Searching for similar reports...',
      aiLoadingPreparing: 'Preparing your report...',
      aiUnavailableManualFallback: 'AI assistance is temporarily unavailable. You can still submit your issue manually.',
      aiUnderstoodReport: 'AI understood your report',
      aiUsedFallbackReviewCarefully: '(AI service unavailable - used a simple keyword-based fallback. Review the fields below carefully.)',
      affectedGroupLabel: 'Affected Group', aiSummaryLabel: 'AI Summary',
      possibleExistingIssue: 'Possible Existing Issue', residentsAffected: 'resident(s) affected',
      iHaveThisProblemToo: 'I Have This Problem Too', viewIssue: 'View Issue',
      createNewIssueAnyway: 'Create New Issue Anyway', nowFollowingIssue: 'You are now following this issue.',
      submitIssue: 'Submit Issue', issueReported: 'Your issue has been reported.',

      // Issue detail
      backToIssues: 'Back to issues', reportedBy: 'Reported by', adminActions: 'Admin Actions',
      changeStatusPlaceholder: 'Change status...', updatePriority: 'Update Priority',
      commentsLabel: 'Comments', issueTimeline: 'Issue Timeline', noActivityYet: 'No activity yet.',
      youAreNowSupporting: 'You are now supporting this issue.', statusUpdated: 'Status updated.',
      priorityUpdated: 'Priority updated.', unableToLoadIssueTitle: 'Unable to load this issue',
      noIssueSpecified: 'No issue specified.',

      // Feed
      feedSubtitle: 'Announcements, discussions, and updates from your neighbors.',
      newPostBtn: 'New Post', allTypes: 'All types',
      feedEmptyMessage: 'Be the first to share something with your community.',
      unableToLoadFeed: 'Unable to load feed: ', pinnedLabel: 'Pinned', unpin: 'Unpin', pin: 'Pin',
      failedToLoadComments: 'Failed to load comments.', deletePostConfirmTitle: 'Delete post?',
      deletePostConfirmMessage: 'This cannot be undone.', postDeleted: 'Post deleted.',
      createAPost: 'Create a Post', announcementAssistant: 'Announcement Assistant',
      announcementAssistantHint: 'Jot down rough notes - AI drafts a polished title & body you can edit before publishing.',
      aiNotesPlaceholder: 'e.g. water shut off tomorrow 10am-2pm for maintenance',
      draftWithAi: 'Draft with AI', titleOptional: 'Title (optional)', publish: 'Publish',
      pleaseWriteSomething: 'Please write something first.', postPublished: 'Post published.',
      typeRoughNotesFirst: 'Type a few rough notes first.', drafting: 'Drafting...',
      draftReadyFallback: 'Draft ready (AI unavailable - used a simple rewrite). Review before publishing.',
      draftReadyReview: 'Draft ready. Review and edit before publishing.',
      couldNotDraftWithAi: 'Could not draft with AI: ',

      // Polls
      pollsTitle: 'Polls', pollsSubtitle: 'Vote on decisions that affect your community.',
      newPollBtn: 'New Poll', noPollsYetTitle: 'No polls yet',
      noPollsYetMessage: "Start a poll to gather your community's opinion.",
      unableToLoadPolls: 'Unable to load polls: ', closedLabel: 'Closed', activeLabel: 'Active',
      votesCount: 'vote(s)', closesOn: 'Closes', noExpiry: 'No expiry', voteRecorded: 'Vote recorded.',
      createAPoll: 'Create a Poll', optionN: 'Option {n}', addOption: '+ Add option',
      expiresOptional: 'Expires (optional)', createPollBtn: 'Create Poll',
      addQuestionAndOptions: 'Add a question and at least 2 options.', pollCreated: 'Poll created.',

      // Events
      eventsTitle: 'Events', eventsSubtitle: 'Meetings, cleanups, and activities in your community.',
      newEventBtn: 'New Event', upcomingTab: 'Upcoming', pastTab: 'Past',
      noEventsFilteredTitle: 'No {mode} events', checkBackOrCreate: 'Check back later or create one yourself.',
      unableToLoadEvents: 'Unable to load events: ', noLocationShort: 'No location',
      goingLabel: 'going', capacitySuffix: ' / {capacity} capacity', maybeLabel: 'maybe',
      statusGoing: '✓ Going', statusMaybe: '? Maybe', statusNotGoing: "✕ Can't go",
      createAnEvent: 'Create an Event', locationLabel: 'Location', startLabel: 'Start', endLabel: 'End',
      capacityOptional: 'Capacity (optional)', createEventBtn: 'Create Event',
      titleStartEndRequired: 'Title, start, and end time are required.', eventCreated: 'Event created.',

      // Lost & Found
      lostFoundSubtitle: 'Reunite lost items with their owners.', reportItemBtn: 'Report Item',
      allLabel: 'All', lostLabel: 'Lost', foundLabel: 'Found', anyStatus: 'Any status',
      photoOptionalLabel: 'Photo (optional)', uploadingPhoto: 'Uploading photo...', avatarLabel: 'Avatar',
      changePhoto: 'Change photo', uploadFailed: 'Photo upload failed: ',
      statusActive: 'Active', statusClaimed: 'Claimed', statusReturned: 'Returned', statusClosed: 'Closed',
      noItemsFoundTitle: 'Nothing here', noItemsFoundMessage: 'No lost or found items match these filters.',
      unableToLoadItems: 'Unable to load items: ', noPhoto: 'No photo', updateStatusPlaceholder: 'Update status...',
      reportLostFoundTitle: 'Report a Lost or Found Item', lostFoundTitlePlaceholder: 'e.g. Lost Cat',
      submit: 'Submit', pleaseAddTitle: 'Please add a title.', itemReported: 'Item reported.',

      // Services / provider marketplace
      localServicesTitle: 'Local Services', localServicesSubtitle: 'Find trusted, rated providers in your community.',
      manageProviderProfile: 'Manage Provider Profile', browseProviders: 'Browse Providers',
      incomingRequests: 'Incoming Requests', myRequests: 'My Requests', allCategories: 'All categories',
      verifiedOnly: 'Verified only', noProvidersFoundTitle: 'No providers found',
      tryDifferentFilter: 'Try a different category or filter.', unableToLoadProviders: 'Unable to load providers: ',
      verifiedBadge: '✓ Verified', reviewsCount: 'reviews', noRequestsYetTitle: 'No requests yet',
      incomingRequestsWillAppear: 'Incoming requests will appear here.',
      requestServiceToSeeHere: 'Request a service from the marketplace to see it here.',
      unableToLoadRequests: 'Unable to load requests: ', accept: 'Accept', decline: 'Decline',
      startWork: 'Start Work', markCompleted: 'Mark Completed', leaveAReview: 'Leave a Review',
      cancelRequest: 'Cancel Request', requestUpdated: 'Request updated.', ratingLabel: 'Rating',
      commentLabel: 'Comment', submitReview: 'Submit Review', reviewSubmittedThankYou: 'Review submitted. Thank you!',
      updateProviderProfile: 'Update Provider Profile', createProviderProfile: 'Create Provider Profile',
      businessNameLabel: 'Business Name', bioLabel: 'Bio', serviceAreaLabel: 'Service Area',
      categoriesLabel: 'Categories', providerProfileSaved: 'Provider profile saved.',

      // Provider detail
      noProviderSpecified: 'No provider specified.', unableToLoadProvider: 'Unable to load provider',
      backToServices: 'Back to services', requestServiceBtn: 'Request Service',
      noDescriptionProvided: 'No description provided.', serviceAreaColon: 'Service area:',
      notSpecified: 'Not specified', reviewsHeader: 'Reviews', noReviewsYet: 'No reviews yet.',
      requestServiceFrom: 'Request Service from {name}', describeWhatYouNeed: 'Describe what you need',
      pleaseDescribeWhatYouNeed: 'Please describe what you need.', sendRequest: 'Send Request',
      serviceRequestSent: 'Service request sent.',

      // Notifications
      notificationsSubtitle: 'Everything that needs your attention.', markAllRead: 'Mark all read',
      noNotificationsTitle: 'No notifications', allCaughtUp: 'You are all caught up.',
      markAsRead: 'Mark as read', unableToLoadNotifications: 'Unable to load notifications: ',
      allNotificationsMarkedRead: 'All notifications marked as read.',

      // Shared nav (app.js): admin section, bottom nav, dropdown menu, topbar
      adminSectionLabel: 'Admin', operationsCenter: 'Operations Center',
      navHome: 'Home', navReport: 'Report', navCommunity: 'Community',
      dropdownProfile: 'Profile', dropdownNotifications: 'Notifications', dropdownLogout: 'Log out',
      loadingCommunities: 'Loading...', noCommunitiesYet: 'No communities yet',
      searchPlaceholder: 'Search issues, posts, providers...',

      // Profile page
      manageAccountDetails: 'Manage your account details.',
      personalInformation: 'Personal Information', avatarUrlOptional: 'Avatar URL (optional)',
      preferredLanguageLabel: 'Preferred language', saveChanges: 'Save Changes',
      accountLabel: 'Account', roleLabel: 'Role', changePasswordHeader: 'Change Password',
      currentPasswordLabel: 'Current password', newPasswordLabel: 'New password',
      updatePasswordBtn: 'Update Password', profileUpdated: 'Profile updated.', passwordUpdated: 'Password updated.',

      // Auth pages
      signInSubtitle: 'Sign in to continue to your community.',
      sessionExpired: 'Your session expired. Please sign in again.',
      showPassword: 'Show', hidePassword: 'Hide',
      exploreDemo: '✨ Explore Demo', noAccountYet: "Don't have an account?",
      alreadyHaveAccount: 'Already have an account?',
      exploreDemoTitle: 'Explore Yaxın.az as a demo user',
      exploreDemoIntro: 'Jump straight into the app without registering. Pick a role to see the platform from that perspective.',
      demoResidentDesc: 'Report issues, join the feed, vote in polls',
      demoCommunityAdminDesc: 'Manage issues, members, and announcements',
      demoServiceProviderDesc: 'View and respond to service requests',
      demoPlatformAdminDesc: 'Operations Center, audit log, moderation',
      roleResident: 'Resident', roleCommunityAdmin: 'Community Admin',
      roleServiceProvider: 'Service Provider', rolePlatformAdmin: 'Platform Admin',
      demoLoginFailed: 'Demo login failed: ',
      joinYourCommunitySubtitle: "Create your Yaxın.az account - it takes less than a minute.",
      atLeast8Chars: 'At least 8 characters.', welcomeToApp: 'Welcome to Yaxın.az!',

      // Admin pages
      adminAccessRequired: 'Admin access required.', adminDashboardTitle: 'Admin Dashboard',
      adminDashboardSubtitle: 'Community health at a glance.',
      selectCommunityFirst: 'Select or create a community first.',
      pleaseTryAgainLater: 'Please try again later.',
      criticalIssuesLabel: 'Critical Issues', staleIssuesLabel: 'Stale Issues', escalatedLabel: 'Escalated', staleLabel: 'Stale',
      resolutionRateLabel: 'Resolution Rate', avgResolutionHrsLabel: 'Avg Resolution (hrs)',
      providersPlatformLabel: 'Providers (platform)', issuesByCategory: 'Issues by Category',
      issuesByPriority: 'Issues by Priority', activityTrend7Day: '7-Day Activity Trend',
      aiCommunityInsights: 'AI Community Insights', notEnoughDataInsights: 'Not enough data yet for meaningful insights.',
      pendingMemberships: 'Pending Memberships', pendingProvidersLabel: 'Pending Providers', noPendingRequests: 'No pending requests.',
      approve: 'Approve', reject: 'Reject', statusApprovedWord: 'approved', statusRejectedWord: 'rejected',
      chartsUnavailable: 'Charts are temporarily unavailable.',
      membershipStatusUpdated: 'Membership {status}.', pendingProviderVerification: 'Pending Provider Verification',
      noPendingProviders: 'No pending providers.', verify: 'Verify', moderationQueue: 'Moderation Queue',
      noPendingReports: 'No pending reports.', review: 'Review', providerVerified: 'Provider verified.',
      unableToLoadAdminWidgets: 'Unable to load admin widgets.',
      operationsCenterSubtitle: 'Real-time platform health and critical items.', refresh: '↻ Refresh',
      escalatedIssuesLabel: 'Escalated Issues', moderationQueueLabel: 'Moderation Queue',
      systemHealth: 'System Health', applicationLabel: 'Application', databaseLabel: 'Database',
      aiProviderLabel: 'AI Provider', webSocketLabel: 'WebSocket', activeProfileLabel: 'Active profile',
      lastChecked: 'Last checked', reviewFromAdminDashboard: "Review from each community's Admin Dashboard.",
      reviewFromProviderWidget: "Review from the Admin Dashboard's provider widget.",
      moderationReportsLabel: 'Moderation Reports', takeAction: 'Take Action', dismiss: 'Dismiss',
      recentAuditTrail: 'Recent Audit Trail', noAuditEntriesYet: 'No audit entries yet.',
      reportUpdated: 'Report updated.', platformAdminAccessRequired: 'Platform admin access required',
      operationsCenterPlatformOnly: 'The Operations Center is only available to platform administrators.',
      unableToLoadOperationsCenter: 'Unable to load Operations Center',

      // Landing page
      landingSignIn: 'Sign in', landingJoinCommunity: 'Join your community',
      landingBadge: '✨ AI-Powered Smart Community Platform',
      landingHeroTitle: 'Your Community.<br/>Smarter. Closer. Connected.',
      landingHeroLead: 'Report problems, connect with neighbors, discover trusted local services and let AI help your community run smarter.',
      landingJoinCta: 'Join Your Community', landingExploreDemo: 'Explore Demo',
      landingWhySection: 'Why Yaxın.az', landingWhyTitle: 'Everything your community needs, in one place',
      landingFeature1Title: 'Smart Issue Reporting',
      landingFeature1Desc: 'Describe a problem in plain language - AI understands it, classifies it, and checks for duplicates instantly.',
      landingFeature2Title: 'AI-Powered Understanding',
      landingFeature2Desc: 'Claude analyzes reports for category, urgency, and affected residents - Java decides what happens next.',
      landingFeature3Title: 'Community Feed',
      landingFeature3Desc: 'Announcements, alerts, and discussions - all in one place, with admin posts clearly distinguished.',
      landingFeature4Title: 'Trusted Local Services',
      landingFeature4Desc: 'Find verified, rated plumbers, electricians, cleaners, and more - right from your community.',
      landingFeature5Title: 'Events & Polls',
      landingFeature5Desc: 'Organize meetings, cleanups, and community decisions with real-time voting and attendance tracking.',
      landingFeature6Title: 'Real-Time Alerts',
      landingFeature6Desc: 'Get notified the moment your issue status changes or a critical announcement is posted.',
      landingFeature7Title: 'Community Analytics',
      landingFeature7Desc: 'Admins see real resolution rates, trends, and AI-generated insights - never guesswork.',
      landingFeature8Title: 'Operations Management',
      landingFeature8Desc: 'An enterprise-style Operations Center for critical issues, escalations, and system health.',
      landingCtaTitle: 'Ready to bring your community online?',
      landingCtaLead: 'Join in minutes, or explore a fully working demo with real seeded data.',
      landingFooter: 'Yaxın.az · Final Java/Spring course project ·',
      landingApiDocs: 'API Docs',

      // Search page
      searchTitle: 'Search', searchSubtitle: 'Search issues, posts, events, lost & found items, and providers.',
      smartSearchBtn: 'Smart Search', smartSearchTitleAttr: 'Let AI figure out what you mean and which sections to look in',
      sectionCommunityPosts: 'Community Posts', sectionServiceProviders: 'Service Providers',
      startTypingToSearch: 'Start typing to search', enterAtLeast2Chars: 'Enter at least 2 characters.',
      smartSearchUnderstood: 'Smart Search understood your query', noResultsFoundTitle: 'No results found',
      nothingMatched: 'Nothing matched "{q}".', searchFailed: 'Search failed: ', smartSearchFailed: 'Smart Search failed: ',
      searchEmptyStateMessage: 'Search issues, posts, events, and more - or click Smart Search to ask in plain language.',
      aiUnavailableFallbackUsed: '(AI service unavailable - used a simple keyword-based fallback.)',

      // Auth pages - left visual panel
      loginVisualHeadline: 'Closer communities.<br/>Smarter living.',
      loginVisualLead: 'Report problems, connect with neighbors, discover trusted local services, and let AI help your community run smarter.',
      loginVisualPoint1: '✓ AI understands your reports instantly',
      loginVisualPoint2: '✓ Real-time updates from your community admins',
      loginVisualPoint3: '✓ Trusted, rated local service providers',
      registerVisualHeadline: 'Your community, one platform away.',
      registerVisualLead: 'Create your account, join your residence, and start participating - reporting issues, joining discussions, and finding trusted local help.',

      // Client-side / backend-error message translation (api.js, form.js)
      errUnableToReachServer: 'Unable to reach the server. Please check your connection.',
      errForbidden: 'You do not have permission to perform this action.',
      errNotFound: 'The requested item could not be found.',
      errConflict: 'This record was updated by another user. Please refresh and try again.',
      errRateLimited: 'You are doing that too often. Please wait a moment and try again.',
      errServerError: 'Something went wrong on our end. Please try again shortly.',
      errGeneric: 'The request could not be completed.',
      errInvalidCredentials: 'Invalid email or password.',
      validationFailed: 'Validation failed',
      valRequired: 'This field is required.',
      valEmailInvalid: 'Enter a valid email address.',
      valMaxLength: 'Must be at most {max} characters.',
      valSizeRange: 'Must be between {min} and {max} characters.',
      valMinValue: 'Must be at least {min}.',
      valMaxValue: 'Must be at most {max}.',
      valPositive: 'Must be a positive number.',

      // Backend enum value labels - see i18n.enumLabel(group, value). Falls back to a prettified
      // (title-cased, underscore-to-space) version of the raw value for anything not listed here.
      'enum.PostType.GENERAL': 'General', 'enum.PostType.ANNOUNCEMENT': 'Announcement',
      'enum.PostType.EVENT': 'Event', 'enum.PostType.QUESTION': 'Question',
      'enum.PostType.ALERT': 'Alert', 'enum.PostType.MARKETPLACE': 'Marketplace',

      'enum.IssueStatus.OPEN': 'Open', 'enum.IssueStatus.ACKNOWLEDGED': 'Acknowledged',
      'enum.IssueStatus.IN_PROGRESS': 'In Progress', 'enum.IssueStatus.WAITING_FOR_VENDOR': 'Waiting for Vendor',
      'enum.IssueStatus.RESOLVED': 'Resolved', 'enum.IssueStatus.CLOSED': 'Closed',
      'enum.IssueStatus.REJECTED': 'Rejected',

      'enum.IssuePriority.LOW': 'Low', 'enum.IssuePriority.MEDIUM': 'Medium',
      'enum.IssuePriority.HIGH': 'High', 'enum.IssuePriority.CRITICAL': 'Critical',

      'enum.IssueCategory.ELEVATOR': 'Elevator', 'enum.IssueCategory.WATER': 'Water',
      'enum.IssueCategory.ELECTRICITY': 'Electricity', 'enum.IssueCategory.GAS': 'Gas',
      'enum.IssueCategory.HEATING': 'Heating', 'enum.IssueCategory.PARKING': 'Parking',
      'enum.IssueCategory.SECURITY': 'Security', 'enum.IssueCategory.NOISE': 'Noise',
      'enum.IssueCategory.CLEANING': 'Cleaning', 'enum.IssueCategory.WASTE': 'Waste',
      'enum.IssueCategory.INTERNET': 'Internet', 'enum.IssueCategory.BUILDING_DAMAGE': 'Building Damage',
      'enum.IssueCategory.ROAD': 'Road', 'enum.IssueCategory.LIGHTING': 'Lighting',
      'enum.IssueCategory.ANIMAL': 'Animal', 'enum.IssueCategory.ACCESSIBILITY': 'Accessibility',
      'enum.IssueCategory.OTHER': 'Other',

      'enum.AttendanceStatus.GOING': 'Going', 'enum.AttendanceStatus.MAYBE': 'Maybe',
      'enum.AttendanceStatus.NOT_GOING': 'Not Going',

      'enum.LostFoundType.LOST': 'Lost', 'enum.LostFoundType.FOUND': 'Found',

      'enum.LostFoundStatus.ACTIVE': 'Active', 'enum.LostFoundStatus.CLAIMED': 'Claimed',
      'enum.LostFoundStatus.RETURNED': 'Returned', 'enum.LostFoundStatus.CLOSED': 'Closed',

      'enum.ServiceRequestStatus.REQUESTED': 'Requested', 'enum.ServiceRequestStatus.ACCEPTED': 'Accepted',
      'enum.ServiceRequestStatus.DECLINED': 'Declined', 'enum.ServiceRequestStatus.IN_PROGRESS': 'In Progress',
      'enum.ServiceRequestStatus.COMPLETED': 'Completed', 'enum.ServiceRequestStatus.CANCELLED': 'Cancelled',

      'enum.ServiceCategory.PLUMBING': 'Plumbing', 'enum.ServiceCategory.ELECTRICAL': 'Electrical',
      'enum.ServiceCategory.CLEANING': 'Cleaning', 'enum.ServiceCategory.HANDYMAN': 'Handyman',
      'enum.ServiceCategory.LOCKSMITH': 'Locksmith', 'enum.ServiceCategory.MOVING': 'Moving',
      'enum.ServiceCategory.APPLIANCE_REPAIR': 'Appliance Repair', 'enum.ServiceCategory.PET_CARE': 'Pet Care',
      'enum.ServiceCategory.TUTORING': 'Tutoring', 'enum.ServiceCategory.CAR_SERVICE': 'Car Service',
      'enum.ServiceCategory.OTHER': 'Other',

      'enum.MembershipStatus.PENDING': 'Pending', 'enum.MembershipStatus.APPROVED': 'Approved',
      'enum.MembershipStatus.REJECTED': 'Rejected', 'enum.MembershipStatus.BLOCKED': 'Blocked',

      'enum.Role.RESIDENT': 'Resident', 'enum.Role.COMMUNITY_ADMIN': 'Community Admin',
      'enum.Role.SERVICE_PROVIDER': 'Service Provider', 'enum.Role.PLATFORM_ADMIN': 'Platform Admin',

      'enum.CommunityType.APARTMENT_BUILDING': 'Apartment Building',
      'enum.CommunityType.RESIDENTIAL_COMPLEX': 'Residential Complex',
      'enum.CommunityType.NEIGHBORHOOD': 'Neighborhood',
      'enum.CommunityType.STUDENT_RESIDENCE': 'Student Residence',
      'enum.CommunityType.PRIVATE_COMMUNITY': 'Private Community',
      'enum.CommunityType.OTHER': 'Other',

      'enum.ContentType.POST': 'Post', 'enum.ContentType.COMMENT': 'Comment',
      'enum.ContentType.ISSUE': 'Issue', 'enum.ContentType.LOST_FOUND_ITEM': 'Lost & Found Item',
      'enum.ContentType.REVIEW': 'Review',

      'enum.ReportReason.SPAM': 'Spam', 'enum.ReportReason.ABUSE': 'Abuse',
      'enum.ReportReason.MISINFORMATION': 'Misinformation',
      'enum.ReportReason.INAPPROPRIATE_CONTENT': 'Inappropriate Content',
      'enum.ReportReason.OTHER': 'Other',
    },
    AZ: {
      dashboard: 'İdarəetmə paneli', community: 'İcma', issues: 'Problemlər', feed: 'Lent',
      polls: 'Sorğular', events: 'Tədbirlər', lostFound: 'İtirilmiş və Tapılmış', services: 'Xidmətlər',
      notifications: 'Bildirişlər', profile: 'Profil', admin: 'Admin', search: 'Axtarış',
      reportProblem: 'Problem Bildir', logout: 'Çıxış', signIn: 'Daxil ol', signUp: 'Qeydiyyat',
      welcomeBack: 'Xoş gəldiniz', joinYourCommunity: 'İcmanıza qoşulun',
      email: 'E-poçt', password: 'Şifrə', firstName: 'Ad', lastName: 'Soyad',
      loading: 'Yüklənir...', save: 'Yadda saxla', cancel: 'Ləğv et', delete: 'Sil', confirm: 'Təsdiqlə',
      noResults: 'Hələ burada heç nə yoxdur.', apply: 'Tətbiq et', close: 'Bağla',

      viewAll: 'Hamısına bax', noLocationSpecified: 'Yer göstərilməyib',
      justNow: 'indicə', minutesAgo: '{n} dəq əvvəl', hoursAgo: '{n} saat əvvəl', daysAgo: '{n} gün əvvəl',
      joinCommunityFirst: 'Əvvəlcə icmaya qoşulun.', noCommunitySelected: 'İcma seçilməyib',
      goToDashboard: 'Panelə keç', prevPage: 'Əvvəlki', nextPage: 'Növbəti',
      pageOf: 'Səhifə {current} / {total}', post: 'Göndər', titleLabel: 'Başlıq',
      categoryLabel: 'Kateqoriya', priorityLabel: 'Prioritet', descriptionLabel: 'Təsvir',
      questionLabel: 'Sual', typeLabel: 'Növ', contentLabel: 'Mətn',
      noCommentsYetTitle: 'Hələ şərh yoxdur', beFirstToComment: 'İlk şərhi siz yazın.',
      writeCommentPlaceholder: 'Şərh yazın...', addCommentPlaceholder: 'Şərh əlavə edin...',

      greetingMorning: 'sabahınız', greetingAfternoon: 'gününüz', greetingEvening: 'axşamınız',
      greetingGood: 'Xeyirli', loadingYourCommunity: 'İcmanız yüklənir...',
      notJoinedCommunity: 'Hələ heç bir icmaya qoşulmamısınız.',
      joinCommunityTitle: 'Başlamaq üçün icmaya qoşulun',
      joinCommunityMessage: 'Problem bildirmək, lenti görmək və s. üçün binanıza baxın və qoşulun.',
      openIssues: 'Açıq Problemlər', resolvedThisWeek: 'Bu Həftə Həll Olunan',
      upcomingEvents: 'Yaxınlaşan Tədbirlər', communityMembers: 'İcma Üzvləri',
      aiCommunitySummary: 'AI İcma Xülasəsi', recentIssues: 'Son Problemlər',
      communityFeed: 'İcma Lenti', recommendedServices: 'Tövsiyə Olunan Xidmətlər',
      noIssuesYetTitle: 'Hələ problem yoxdur', noIssuesYetMessage: 'Bu icmada hələ heç nə bildirilməyib.',
      noPostsYetTitle: 'Hələ paylaşım yoxdur', noPostsYetDashboardMessage: 'İcma lentində ilk paylaşımı siz edin.',
      noEventsYetTitle: 'Yaxınlaşan tədbir yoxdur', noEventsYetMessage: 'İcma tədbirləri üçün tezliklə yoxlayın.',
      noProvidersYetTitle: 'Hələ provayder yoxdur', noProvidersYetMessage: 'Təsdiqlənmiş provayderlər burada görünəcək.',
      unableToLoadStats: 'Statistika yüklənə bilmədi.',
      alertNeedsAttention: '{critical} kritik və {escalated} eskalasiya olunmuş problem diqqət tələb edir.',
      reviewNow: 'İndi bax', aiUnavailableShort: 'AI yardımı müvəqqəti əlçatan deyil.',
      unableToLoadDashboard: 'Panel yüklənə bilmədi: ',

      issuesTitle: 'Problemlər', issuesSubtitle: 'İcmanızda bildirilən hər şey.',
      staleOnly: 'Yalnız köhnəlmiş', escalatedOnly: 'Yalnız eskalasiya olunmuş',
      joinCommunityFromDashboard: 'Əvvəlcə paneldən icmaya qoşulun.',
      noIssuesFoundTitle: 'Problem tapılmadı', noIssuesFoundMessage: 'Bu filtrlərə hazırda heç nə uyğun gəlmir.',
      unableToLoadIssues: 'Problemlər yüklənə bilmədi: ',
      whatHappened: 'Nə baş verib?', describeProblemPlaceholder: 'Problemi öz sözlərinizlə təsvir edin...',
      buildingLocationOptional: 'Bina / yer (istəyə bağlı)', buildingLocationPlaceholder: 'məs. B binası',
      analyzeAndReport: 'Analiz Et və Bildir', pleaseDescribeMore: 'Zəhmət olmasa problemi bir az daha ətraflı təsvir edin.',
      aiLoadingUnderstanding: 'Hesabatınız başa düşülür...', aiLoadingCategory: 'Problem kateqoriyası müəyyən edilir...',
      aiLoadingUrgency: 'Təcililik yoxlanılır...', aiLoadingSimilar: 'Oxşar hesabatlar axtarılır...',
      aiLoadingPreparing: 'Hesabatınız hazırlanır...',
      aiUnavailableManualFallback: 'AI yardımı müvəqqəti əlçatan deyil. Problemi hələ də əl ilə göndərə bilərsiniz.',
      aiUnderstoodReport: 'AI hesabatınızı başa düşdü',
      aiUsedFallbackReviewCarefully: '(AI xidməti əlçatan deyil - sadə açar-söz əsaslı ehtiyat üsulu istifadə olundu. Aşağıdakı sahələri diqqətlə yoxlayın.)',
      affectedGroupLabel: 'Təsirlənən Qrup', aiSummaryLabel: 'AI Xülasəsi',
      possibleExistingIssue: 'Mövcud Ola Bilən Problem', residentsAffected: 'sakin təsirlənib',
      iHaveThisProblemToo: 'Məndə də Bu Problem Var', viewIssue: 'Problemə Bax',
      createNewIssueAnyway: 'Yenə də Yeni Problem Yarat', nowFollowingIssue: 'İndi bu problemi izləyirsiniz.',
      submitIssue: 'Problemi Göndər', issueReported: 'Probleminiz bildirildi.',

      backToIssues: 'Problemlərə qayıt', reportedBy: 'Bildirən', adminActions: 'Admin Əməliyyatları',
      changeStatusPlaceholder: 'Statusu dəyiş...', updatePriority: 'Prioriteti Yenilə',
      commentsLabel: 'Şərhlər', issueTimeline: 'Problem Xronologiyası', noActivityYet: 'Hələ fəaliyyət yoxdur.',
      youAreNowSupporting: 'İndi bu problemi dəstəkləyirsiniz.', statusUpdated: 'Status yeniləndi.',
      priorityUpdated: 'Prioritet yeniləndi.', unableToLoadIssueTitle: 'Bu problem yüklənə bilmədi',
      noIssueSpecified: 'Problem göstərilməyib.',

      feedSubtitle: 'Qonşularınızdan elanlar, müzakirələr və yeniliklər.',
      newPostBtn: 'Yeni Paylaşım', allTypes: 'Bütün növlər',
      feedEmptyMessage: 'İcmanızla nəsə paylaşan ilk şəxs olun.',
      unableToLoadFeed: 'Lent yüklənə bilmədi: ', pinnedLabel: 'Bərkidilib', unpin: 'Bərkitməni çıxar', pin: 'Bərkit',
      failedToLoadComments: 'Şərhlər yüklənmədi.', deletePostConfirmTitle: 'Paylaşım silinsin?',
      deletePostConfirmMessage: 'Bu geri qaytarıla bilməz.', postDeleted: 'Paylaşım silindi.',
      createAPost: 'Paylaşım Yarat', announcementAssistant: 'Elan Köməkçisi',
      announcementAssistantHint: 'Qısa qeydlər yazın - AI dərc etməzdən əvvəl redaktə edə biləcəyiniz mətn hazırlayacaq.',
      aiNotesPlaceholder: 'məs. sabah 10:00-14:00 arası su kəsiləcək, təmir üçün',
      draftWithAi: 'AI ilə Hazırla', titleOptional: 'Başlıq (istəyə bağlı)', publish: 'Dərc Et',
      pleaseWriteSomething: 'Zəhmət olmasa əvvəlcə nəsə yazın.', postPublished: 'Paylaşım dərc edildi.',
      typeRoughNotesFirst: 'Əvvəlcə bir neçə qeyd yazın.', drafting: 'Hazırlanır...',
      draftReadyFallback: 'Layihə hazırdır (AI əlçatan deyil - sadə yenidənyazma istifadə olundu). Dərc etmədən əvvəl yoxlayın.',
      draftReadyReview: 'Layihə hazırdır. Dərc etmədən əvvəl baxın və redaktə edin.',
      couldNotDraftWithAi: 'AI ilə hazırlana bilmədi: ',

      pollsTitle: 'Sorğular', pollsSubtitle: 'İcmanıza təsir edən qərarlara səs verin.',
      newPollBtn: 'Yeni Sorğu', noPollsYetTitle: 'Hələ sorğu yoxdur',
      noPollsYetMessage: 'İcmanızın fikrini öyrənmək üçün sorğu başladın.',
      unableToLoadPolls: 'Sorğular yüklənə bilmədi: ', closedLabel: 'Bağlı', activeLabel: 'Aktiv',
      votesCount: 'səs', closesOn: 'Bağlanır', noExpiry: 'Müddət yoxdur', voteRecorded: 'Səsiniz qeydə alındı.',
      createAPoll: 'Sorğu Yarat', optionN: '{n}-ci variant', addOption: '+ Variant əlavə et',
      expiresOptional: 'Bitmə tarixi (istəyə bağlı)', createPollBtn: 'Sorğu Yarat',
      addQuestionAndOptions: 'Sual və ən azı 2 variant əlavə edin.', pollCreated: 'Sorğu yaradıldı.',

      eventsTitle: 'Tədbirlər', eventsSubtitle: 'İcmanızdakı görüşlər, təmizlik işləri və fəaliyyətlər.',
      newEventBtn: 'Yeni Tədbir', upcomingTab: 'Yaxınlaşan', pastTab: 'Keçmiş',
      noEventsFilteredTitle: '{mode} tədbir yoxdur', checkBackOrCreate: 'Sonra yenidən yoxlayın və ya özünüz bir tədbir yaradın.',
      unableToLoadEvents: 'Tədbirlər yüklənə bilmədi: ', noLocationShort: 'Yer yoxdur',
      goingLabel: 'gedəcək', capacitySuffix: ' / {capacity} tutum', maybeLabel: 'bəlkə',
      statusGoing: '✓ Gedəcəm', statusMaybe: '? Bəlkə', statusNotGoing: '✕ Gedə bilmərəm',
      createAnEvent: 'Tədbir Yarat', locationLabel: 'Yer', startLabel: 'Başlanğıc', endLabel: 'Bitiş',
      capacityOptional: 'Tutum (istəyə bağlı)', createEventBtn: 'Tədbir Yarat',
      titleStartEndRequired: 'Başlıq, başlanğıc və bitiş vaxtı tələb olunur.', eventCreated: 'Tədbir yaradıldı.',

      lostFoundSubtitle: 'İtirilmiş əşyaları sahibləri ilə yenidən birləşdirin.', reportItemBtn: 'Əşya Bildir',
      allLabel: 'Hamısı', lostLabel: 'İtirilib', foundLabel: 'Tapılıb', anyStatus: 'İstənilən status',
      photoOptionalLabel: 'Şəkil (istəyə bağlı)', uploadingPhoto: 'Şəkil yüklənir...', avatarLabel: 'Avatar',
      changePhoto: 'Şəkli dəyiş', uploadFailed: 'Şəkil yüklənmədi: ',
      statusActive: 'Aktiv', statusClaimed: 'Tələb olunub', statusReturned: 'Qaytarılıb', statusClosed: 'Bağlı',
      noItemsFoundTitle: 'Burada heç nə yoxdur', noItemsFoundMessage: 'Bu filtrlərə uyğun itirilmiş/tapılmış əşya yoxdur.',
      unableToLoadItems: 'Əşyalar yüklənə bilmədi: ', noPhoto: 'Şəkil yoxdur', updateStatusPlaceholder: 'Statusu yenilə...',
      reportLostFoundTitle: 'İtirilmiş və ya Tapılmış Əşya Bildir', lostFoundTitlePlaceholder: 'məs. İtirilmiş Pişik',
      submit: 'Göndər', pleaseAddTitle: 'Zəhmət olmasa başlıq əlavə edin.', itemReported: 'Əşya bildirildi.',

      localServicesTitle: 'Yerli Xidmətlər', localServicesSubtitle: 'İcmanızda etibarlı, reytinqli provayderlər tapın.',
      manageProviderProfile: 'Provayder Profilini İdarə Et', browseProviders: 'Provayderlərə Bax',
      incomingRequests: 'Gələn Sorğular', myRequests: 'Mənim Sorğularım', allCategories: 'Bütün kateqoriyalar',
      verifiedOnly: 'Yalnız təsdiqlənmiş', noProvidersFoundTitle: 'Provayder tapılmadı',
      tryDifferentFilter: 'Fərqli kateqoriya və ya filtr sınayın.', unableToLoadProviders: 'Provayderlər yüklənə bilmədi: ',
      verifiedBadge: '✓ Təsdiqlənib', reviewsCount: 'rəy', noRequestsYetTitle: 'Hələ sorğu yoxdur',
      incomingRequestsWillAppear: 'Gələn sorğular burada görünəcək.',
      requestServiceToSeeHere: 'Burada görmək üçün bazardan bir xidmət tələb edin.',
      unableToLoadRequests: 'Sorğular yüklənə bilmədi: ', accept: 'Qəbul et', decline: 'Rədd et',
      startWork: 'İşə Başla', markCompleted: 'Tamamlanmış Kimi Qeyd Et', leaveAReview: 'Rəy Bildir',
      cancelRequest: 'Sorğunu Ləğv Et', requestUpdated: 'Sorğu yeniləndi.', ratingLabel: 'Reytinq',
      commentLabel: 'Şərh', submitReview: 'Rəyi Göndər', reviewSubmittedThankYou: 'Rəyiniz göndərildi. Təşəkkürlər!',
      updateProviderProfile: 'Provayder Profilini Yenilə', createProviderProfile: 'Provayder Profili Yarat',
      businessNameLabel: 'Biznes Adı', bioLabel: 'Bio', serviceAreaLabel: 'Xidmət Ərazisi',
      categoriesLabel: 'Kateqoriyalar', providerProfileSaved: 'Provayder profili yadda saxlanıldı.',

      noProviderSpecified: 'Provayder göstərilməyib.', unableToLoadProvider: 'Provayder yüklənə bilmədi',
      backToServices: 'Xidmətlərə qayıt', requestServiceBtn: 'Xidmət Tələb Et',
      noDescriptionProvided: 'Təsvir verilməyib.', serviceAreaColon: 'Xidmət ərazisi:',
      notSpecified: 'Göstərilməyib', reviewsHeader: 'Rəylər', noReviewsYet: 'Hələ rəy yoxdur.',
      requestServiceFrom: '{name}-dən Xidmət Tələb Et', describeWhatYouNeed: 'Nəyə ehtiyacınız olduğunu təsvir edin',
      pleaseDescribeWhatYouNeed: 'Zəhmət olmasa nəyə ehtiyacınız olduğunu təsvir edin.', sendRequest: 'Sorğu Göndər',
      serviceRequestSent: 'Xidmət sorğusu göndərildi.',

      notificationsSubtitle: 'Diqqətinizi tələb edən hər şey.', markAllRead: 'Hamısını oxunmuş et',
      noNotificationsTitle: 'Bildiriş yoxdur', allCaughtUp: 'Hər şeyi görmüsünüz.',
      markAsRead: 'Oxunmuş kimi qeyd et', unableToLoadNotifications: 'Bildirişlər yüklənə bilmədi: ',
      allNotificationsMarkedRead: 'Bütün bildirişlər oxunmuş kimi qeyd edildi.',

      adminSectionLabel: 'Admin', operationsCenter: 'Əməliyyat Mərkəzi',
      navHome: 'Ana səhifə', navReport: 'Bildir', navCommunity: 'İcma',
      dropdownProfile: 'Profil', dropdownNotifications: 'Bildirişlər', dropdownLogout: 'Çıxış',
      loadingCommunities: 'Yüklənir...', noCommunitiesYet: 'Hələ icma yoxdur',
      searchPlaceholder: 'Problemlər, paylaşımlar, provayderlər axtarın...',

      manageAccountDetails: 'Hesab məlumatlarınızı idarə edin.',
      personalInformation: 'Şəxsi Məlumatlar', avatarUrlOptional: 'Avatar URL (istəyə bağlı)',
      preferredLanguageLabel: 'Seçilmiş dil', saveChanges: 'Dəyişiklikləri Yadda Saxla',
      accountLabel: 'Hesab', roleLabel: 'Rol', changePasswordHeader: 'Şifrəni Dəyiş',
      currentPasswordLabel: 'Cari şifrə', newPasswordLabel: 'Yeni şifrə',
      updatePasswordBtn: 'Şifrəni Yenilə', profileUpdated: 'Profil yeniləndi.', passwordUpdated: 'Şifrə yeniləndi.',

      signInSubtitle: 'İcmanıza davam etmək üçün daxil olun.',
      sessionExpired: 'Sessiyanızın müddəti bitib. Zəhmət olmasa yenidən daxil olun.',
      showPassword: 'Göstər', hidePassword: 'Gizlət',
      exploreDemo: '✨ Demoya Bax', noAccountYet: 'Hesabınız yoxdur?',
      alreadyHaveAccount: 'Artıq hesabınız var?',
      exploreDemoTitle: 'Yaxın.az-ı demo istifadəçi kimi kəşf edin',
      exploreDemoIntro: 'Qeydiyyatdan keçmədən birbaşa tətbiqə keçin. Platformaya həmin baxış bucağından baxmaq üçün rol seçin.',
      demoResidentDesc: 'Problemləri bildirin, lentə qoşulun, sorğularda səs verin',
      demoCommunityAdminDesc: 'Problemləri, üzvləri və elanları idarə edin',
      demoServiceProviderDesc: 'Xidmət sorğularına baxın və cavab verin',
      demoPlatformAdminDesc: 'Əməliyyat Mərkəzi, audit jurnalı, moderasiya',
      roleResident: 'Sakin', roleCommunityAdmin: 'İcma Admini',
      roleServiceProvider: 'Xidmət Provayderi', rolePlatformAdmin: 'Platforma Admini',
      demoLoginFailed: 'Demo girişi uğursuz oldu: ',
      joinYourCommunitySubtitle: 'Yaxın.az hesabınızı yaradın - bu, bir dəqiqədən az vaxt alır.',
      atLeast8Chars: 'Ən azı 8 simvol.', welcomeToApp: 'Yaxın.az-a xoş gəldiniz!',

      adminAccessRequired: 'Admin girişi tələb olunur.', adminDashboardTitle: 'Admin Paneli',
      adminDashboardSubtitle: 'İcmanın vəziyyətinə ümumi baxış.',
      selectCommunityFirst: 'Əvvəlcə icma seçin və ya yaradın.',
      pleaseTryAgainLater: 'Zəhmət olmasa sonra yenidən cəhd edin.',
      criticalIssuesLabel: 'Kritik Problemlər', staleIssuesLabel: 'Köhnəlmiş Problemlər', escalatedLabel: 'Eskalasiya Olunmuş', staleLabel: 'Köhnəlmiş',
      resolutionRateLabel: 'Həll Nisbəti', avgResolutionHrsLabel: 'Orta Həll Vaxtı (saat)',
      providersPlatformLabel: 'Provayderlər (platforma)', issuesByCategory: 'Kateqoriyaya Görə Problemlər',
      issuesByPriority: 'Prioritetə Görə Problemlər', activityTrend7Day: '7 Günlük Fəaliyyət Trendi',
      aiCommunityInsights: 'AI İcma Anlayışları', notEnoughDataInsights: 'Mənalı anlayışlar üçün hələ kifayət qədər məlumat yoxdur.',
      pendingMemberships: 'Gözləyən Üzvlüklər', pendingProvidersLabel: 'Gözləyən Provayderlər', noPendingRequests: 'Gözləyən sorğu yoxdur.',
      approve: 'Təsdiqlə', reject: 'Rədd et', statusApprovedWord: 'təsdiqləndi', statusRejectedWord: 'rədd edildi',
      chartsUnavailable: 'Qrafiklər müvəqqəti əlçatan deyil.',
      membershipStatusUpdated: 'Üzvlük {status}.', pendingProviderVerification: 'Təsdiq Gözləyən Provayderlər',
      noPendingProviders: 'Gözləyən provayder yoxdur.', verify: 'Təsdiqlə', moderationQueue: 'Moderasiya Növbəsi',
      noPendingReports: 'Gözləyən şikayət yoxdur.', review: 'Bax', providerVerified: 'Provayder təsdiqləndi.',
      unableToLoadAdminWidgets: 'Admin vidcetləri yüklənə bilmədi.',
      operationsCenterSubtitle: 'Real vaxtda platforma sağlamlığı və kritik məsələlər.', refresh: '↻ Yenilə',
      escalatedIssuesLabel: 'Eskalasiya Olunmuş Problemlər', moderationQueueLabel: 'Moderasiya Növbəsi',
      systemHealth: 'Sistem Sağlamlığı', applicationLabel: 'Tətbiq', databaseLabel: 'Verilənlər Bazası',
      aiProviderLabel: 'AI Provayderi', webSocketLabel: 'WebSocket', activeProfileLabel: 'Aktiv profil',
      lastChecked: 'Son yoxlanılıb', reviewFromAdminDashboard: 'Hər icmanın Admin Panelindən baxın.',
      reviewFromProviderWidget: 'Admin Panelinin provayder vidcetindən baxın.',
      moderationReportsLabel: 'Moderasiya Şikayətləri', takeAction: 'Tədbir Gör', dismiss: 'Rədd Et',
      recentAuditTrail: 'Son Audit İzi', noAuditEntriesYet: 'Hələ audit qeydi yoxdur.',
      reportUpdated: 'Şikayət yeniləndi.', platformAdminAccessRequired: 'Platforma admin girişi tələb olunur',
      operationsCenterPlatformOnly: 'Əməliyyat Mərkəzi yalnız platforma adminləri üçün əlçatandır.',
      unableToLoadOperationsCenter: 'Əməliyyat Mərkəzi yüklənə bilmədi',

      landingSignIn: 'Daxil ol', landingJoinCommunity: 'İcmanıza qoşulun',
      landingBadge: '✨ AI Dəstəkli Ağıllı İcma Platforması',
      landingHeroTitle: 'İcmanız.<br/>Daha Ağıllı. Daha Yaxın. Bağlı.',
      landingHeroLead: 'Problemləri bildirin, qonşularınızla əlaqə saxlayın, etibarlı yerli xidmətlər tapın və AI-a icmanızın daha ağıllı işləməsinə kömək etməyə icazə verin.',
      landingJoinCta: 'İcmanıza Qoşulun', landingExploreDemo: 'Demoya Bax',
      landingWhySection: 'Niyə Yaxın.az', landingWhyTitle: 'İcmanızın ehtiyac duyduğu hər şey, bir yerdə',
      landingFeature1Title: 'Ağıllı Problem Bildirişi',
      landingFeature1Desc: 'Problemi sadə dildə təsvir edin - AI onu başa düşür, təsnif edir və təkrarları anında yoxlayır.',
      landingFeature2Title: 'AI Dəstəkli Anlayış',
      landingFeature2Desc: 'Claude hesabatları kateqoriya, təcililik və təsirlənən sakinlər üçün analiz edir - Java növbəti addımı müəyyən edir.',
      landingFeature3Title: 'İcma Lenti',
      landingFeature3Desc: 'Elanlar, xəbərdarlıqlar və müzakirələr - hamısı bir yerdə, admin paylaşımları aydın şəkildə fərqləndirilir.',
      landingFeature4Title: 'Etibarlı Yerli Xidmətlər',
      landingFeature4Desc: 'Təsdiqlənmiş, reytinqli santexniklər, elektriklər, təmizlikçilər və daha çoxunu - birbaşa icmanızdan tapın.',
      landingFeature5Title: 'Tədbirlər və Sorğular',
      landingFeature5Desc: 'Real vaxtda səsvermə və iştirak izləmə ilə görüşlər, təmizlik işləri və icma qərarlarını təşkil edin.',
      landingFeature6Title: 'Real Vaxt Xəbərdarlıqları',
      landingFeature6Desc: 'Probleminizin statusu dəyişən kimi və ya kritik elan dərc olunan kimi bildiriş alın.',
      landingFeature7Title: 'İcma Analitikası',
      landingFeature7Desc: 'Adminlər real həll nisbətlərini, trendləri və AI tərəfindən yaradılan anlayışları görür - heç vaxt təxmin deyil.',
      landingFeature8Title: 'Əməliyyat İdarəetməsi',
      landingFeature8Desc: 'Kritik problemlər, eskalasiyalar və sistem sağlamlığı üçün korporativ tərzdə Əməliyyat Mərkəzi.',
      landingCtaTitle: 'İcmanızı onlayn etməyə hazırsınız?',
      landingCtaLead: 'Dəqiqələr ərzində qoşulun, ya da real seed məlumatları ilə tam işləyən demoya baxın.',
      landingFooter: 'Yaxın.az · Yekun Java/Spring kurs layihəsi ·',
      landingApiDocs: 'API Sənədləri',

      searchTitle: 'Axtarış', searchSubtitle: 'Problemləri, paylaşımları, tədbirləri, itirilmiş/tapılmış əşyaları və provayderləri axtarın.',
      smartSearchBtn: 'Ağıllı Axtarış', smartSearchTitleAttr: 'AI-a nə demək istədiyinizi və hansı bölmələrə baxacağını başa düşməsinə icazə verin',
      sectionCommunityPosts: 'İcma Paylaşımları', sectionServiceProviders: 'Xidmət Provayderləri',
      startTypingToSearch: 'Axtarmaq üçün yazmağa başlayın', enterAtLeast2Chars: 'Ən azı 2 simvol daxil edin.',
      smartSearchUnderstood: 'Ağıllı Axtarış sorğunuzu başa düşdü', noResultsFoundTitle: 'Nəticə tapılmadı',
      nothingMatched: '"{q}" heç nəyə uyğun gəlmədi.', searchFailed: 'Axtarış uğursuz oldu: ', smartSearchFailed: 'Ağıllı Axtarış uğursuz oldu: ',
      searchEmptyStateMessage: 'Problemləri, paylaşımları, tədbirləri və s. axtarın - və ya sadə dildə soruşmaq üçün Ağıllı Axtarışa klikləyin.',
      aiUnavailableFallbackUsed: '(AI xidməti əlçatan deyil - sadə açar-söz əsaslı ehtiyat üsulu istifadə olundu.)',

      loginVisualHeadline: 'Daha yaxın icmalar.<br/>Daha ağıllı həyat.',
      loginVisualLead: 'Problemləri bildirin, qonşularınızla əlaqə saxlayın, etibarlı yerli xidmətlər tapın və AI-a icmanızın daha ağıllı işləməsinə kömək etməyə icazə verin.',
      loginVisualPoint1: '✓ AI hesabatlarınızı anında başa düşür',
      loginVisualPoint2: '✓ İcma adminlərinizdən real vaxtda yeniləmələr',
      loginVisualPoint3: '✓ Etibarlı, reytinqli yerli xidmət provayderləri',
      registerVisualHeadline: 'İcmanız, bir platforma uzağınızda.',
      registerVisualLead: 'Hesabınızı yaradın, binanıza qoşulun və iştiraka başlayın - problemləri bildirin, müzakirələrə qoşulun və etibarlı yerli kömək tapın.',

      errUnableToReachServer: 'Serverə qoşulmaq mümkün olmadı. Zəhmət olmasa internet bağlantınızı yoxlayın.',
      errForbidden: 'Bu əməliyyatı yerinə yetirmək üçün icazəniz yoxdur.',
      errNotFound: 'Axtarılan element tapılmadı.',
      errConflict: 'Bu qeyd başqa istifadəçi tərəfindən yenilənib. Zəhmət olmasa səhifəni yeniləyib yenidən cəhd edin.',
      errRateLimited: 'Siz bunu çox tez-tez edirsiniz. Bir az gözləyib yenidən cəhd edin.',
      errServerError: 'Bizim tərəfdə xəta baş verdi. Zəhmət olmasa bir az sonra yenidən cəhd edin.',
      errGeneric: 'Sorğu tamamlana bilmədi.',
      errInvalidCredentials: 'E-poçt və ya şifrə yanlışdır.',
      validationFailed: 'Doğrulama uğursuz oldu',
      valRequired: 'Bu sahə mütləqdir.',
      valEmailInvalid: 'Düzgün e-poçt ünvanı daxil edin.',
      valMaxLength: 'Ən çoxu {max} simvol ola bilər.',
      valSizeRange: '{min} ilə {max} simvol arasında olmalıdır.',
      valMinValue: 'Ən azı {min} olmalıdır.',
      valMaxValue: 'Ən çoxu {max} ola bilər.',
      valPositive: 'Müsbət ədəd olmalıdır.',

      'enum.PostType.GENERAL': 'Ümumi', 'enum.PostType.ANNOUNCEMENT': 'Elan',
      'enum.PostType.EVENT': 'Tədbir', 'enum.PostType.QUESTION': 'Sual',
      'enum.PostType.ALERT': 'Xəbərdarlıq', 'enum.PostType.MARKETPLACE': 'Bazar',

      'enum.IssueStatus.OPEN': 'Açıq', 'enum.IssueStatus.ACKNOWLEDGED': 'Qəbul edilib',
      'enum.IssueStatus.IN_PROGRESS': 'İcra olunur', 'enum.IssueStatus.WAITING_FOR_VENDOR': 'Podratçı gözlənilir',
      'enum.IssueStatus.RESOLVED': 'Həll edilib', 'enum.IssueStatus.CLOSED': 'Bağlanıb',
      'enum.IssueStatus.REJECTED': 'Rədd edilib',

      'enum.IssuePriority.LOW': 'Aşağı', 'enum.IssuePriority.MEDIUM': 'Orta',
      'enum.IssuePriority.HIGH': 'Yüksək', 'enum.IssuePriority.CRITICAL': 'Kritik',

      'enum.IssueCategory.ELEVATOR': 'Lift', 'enum.IssueCategory.WATER': 'Su',
      'enum.IssueCategory.ELECTRICITY': 'Elektrik', 'enum.IssueCategory.GAS': 'Qaz',
      'enum.IssueCategory.HEATING': 'İstilik', 'enum.IssueCategory.PARKING': 'Avtodayanacaq',
      'enum.IssueCategory.SECURITY': 'Təhlükəsizlik', 'enum.IssueCategory.NOISE': 'Səs-küy',
      'enum.IssueCategory.CLEANING': 'Təmizlik', 'enum.IssueCategory.WASTE': 'Tullantı',
      'enum.IssueCategory.INTERNET': 'İnternet', 'enum.IssueCategory.BUILDING_DAMAGE': 'Bina zədəsi',
      'enum.IssueCategory.ROAD': 'Yol', 'enum.IssueCategory.LIGHTING': 'İşıqlandırma',
      'enum.IssueCategory.ANIMAL': 'Heyvan', 'enum.IssueCategory.ACCESSIBILITY': 'Əlçatanlıq',
      'enum.IssueCategory.OTHER': 'Digər',

      'enum.AttendanceStatus.GOING': 'Gəlirəm', 'enum.AttendanceStatus.MAYBE': 'Bəlkə',
      'enum.AttendanceStatus.NOT_GOING': 'Gəlmirəm',

      'enum.LostFoundType.LOST': 'İtirilmiş', 'enum.LostFoundType.FOUND': 'Tapılmış',

      'enum.LostFoundStatus.ACTIVE': 'Aktiv', 'enum.LostFoundStatus.CLAIMED': 'Sahiblənilib',
      'enum.LostFoundStatus.RETURNED': 'Qaytarılıb', 'enum.LostFoundStatus.CLOSED': 'Bağlanıb',

      'enum.ServiceRequestStatus.REQUESTED': 'Sorğu edilib', 'enum.ServiceRequestStatus.ACCEPTED': 'Qəbul edilib',
      'enum.ServiceRequestStatus.DECLINED': 'Rədd edilib', 'enum.ServiceRequestStatus.IN_PROGRESS': 'İcra olunur',
      'enum.ServiceRequestStatus.COMPLETED': 'Tamamlanıb', 'enum.ServiceRequestStatus.CANCELLED': 'Ləğv edilib',

      'enum.ServiceCategory.PLUMBING': 'Santexnika', 'enum.ServiceCategory.ELECTRICAL': 'Elektrik işləri',
      'enum.ServiceCategory.CLEANING': 'Təmizlik', 'enum.ServiceCategory.HANDYMAN': 'Usta',
      'enum.ServiceCategory.LOCKSMITH': 'Kilidçi', 'enum.ServiceCategory.MOVING': 'Daşınma',
      'enum.ServiceCategory.APPLIANCE_REPAIR': 'Məişət texnikası təmiri', 'enum.ServiceCategory.PET_CARE': 'Heyvan qulluğu',
      'enum.ServiceCategory.TUTORING': 'Dərs', 'enum.ServiceCategory.CAR_SERVICE': 'Avtomobil xidməti',
      'enum.ServiceCategory.OTHER': 'Digər',

      'enum.MembershipStatus.PENDING': 'Gözləmədə', 'enum.MembershipStatus.APPROVED': 'Təsdiqlənib',
      'enum.MembershipStatus.REJECTED': 'Rədd edilib', 'enum.MembershipStatus.BLOCKED': 'Bloklanıb',

      'enum.Role.RESIDENT': 'Sakin', 'enum.Role.COMMUNITY_ADMIN': 'İcma Admini',
      'enum.Role.SERVICE_PROVIDER': 'Xidmət Provayderi', 'enum.Role.PLATFORM_ADMIN': 'Platforma Admini',

      'enum.CommunityType.APARTMENT_BUILDING': 'Mənzil binası',
      'enum.CommunityType.RESIDENTIAL_COMPLEX': 'Yaşayış kompleksi',
      'enum.CommunityType.NEIGHBORHOOD': 'Qonşuluq',
      'enum.CommunityType.STUDENT_RESIDENCE': 'Tələbə yataqxanası',
      'enum.CommunityType.PRIVATE_COMMUNITY': 'Özəl icma',
      'enum.CommunityType.OTHER': 'Digər',

      'enum.ContentType.POST': 'Paylaşım', 'enum.ContentType.COMMENT': 'Şərh',
      'enum.ContentType.ISSUE': 'Problem', 'enum.ContentType.LOST_FOUND_ITEM': 'İtirilmiş/Tapılmış əşya',
      'enum.ContentType.REVIEW': 'Rəy',

      'enum.ReportReason.SPAM': 'Spam', 'enum.ReportReason.ABUSE': 'Sui-istifadə',
      'enum.ReportReason.MISINFORMATION': 'Yanlış məlumat',
      'enum.ReportReason.INAPPROPRIATE_CONTENT': 'Yolverilməz məzmun',
      'enum.ReportReason.OTHER': 'Digər',
    },
    RU: {
      dashboard: 'Панель управления', community: 'Сообщество', issues: 'Проблемы', feed: 'Лента',
      polls: 'Опросы', events: 'События', lostFound: 'Бюро находок', services: 'Услуги',
      notifications: 'Уведомления', profile: 'Профиль', admin: 'Админ', search: 'Поиск',
      reportProblem: 'Сообщить о проблеме', logout: 'Выйти', signIn: 'Войти', signUp: 'Регистрация',
      welcomeBack: 'С возвращением', joinYourCommunity: 'Присоединитесь к сообществу',
      email: 'Эл. почта', password: 'Пароль', firstName: 'Имя', lastName: 'Фамилия',
      loading: 'Загрузка...', save: 'Сохранить', cancel: 'Отмена', delete: 'Удалить', confirm: 'Подтвердить',
      noResults: 'Здесь пока ничего нет.', apply: 'Применить', close: 'Закрыть',

      viewAll: 'Смотреть все', noLocationSpecified: 'Место не указано',
      justNow: 'только что', minutesAgo: '{n} мин назад', hoursAgo: '{n} ч назад', daysAgo: '{n} дн назад',
      joinCommunityFirst: 'Сначала присоединитесь к сообществу.', noCommunitySelected: 'Сообщество не выбрано',
      goToDashboard: 'На панель управления', prevPage: 'Назад', nextPage: 'Далее',
      pageOf: 'Страница {current} из {total}', post: 'Отправить', titleLabel: 'Заголовок',
      categoryLabel: 'Категория', priorityLabel: 'Приоритет', descriptionLabel: 'Описание',
      questionLabel: 'Вопрос', typeLabel: 'Тип', contentLabel: 'Содержание',
      noCommentsYetTitle: 'Пока нет комментариев', beFirstToComment: 'Оставьте первый комментарий.',
      writeCommentPlaceholder: 'Написать комментарий...', addCommentPlaceholder: 'Добавить комментарий...',

      greetingMorning: 'утро', greetingAfternoon: 'день', greetingEvening: 'вечер',
      greetingGood: 'Добрый', loadingYourCommunity: 'Загрузка вашего сообщества...',
      notJoinedCommunity: 'Вы ещё не присоединились ни к одному сообществу.',
      joinCommunityTitle: 'Присоединитесь к сообществу, чтобы начать',
      joinCommunityMessage: 'Найдите свой дом и присоединитесь, чтобы сообщать о проблемах, видеть ленту и многое другое.',
      openIssues: 'Открытые проблемы', resolvedThisWeek: 'Решено на этой неделе',
      upcomingEvents: 'Ближайшие события', communityMembers: 'Участники сообщества',
      aiCommunitySummary: 'AI-сводка сообщества', recentIssues: 'Недавние проблемы',
      communityFeed: 'Лента сообщества', recommendedServices: 'Рекомендуемые услуги',
      noIssuesYetTitle: 'Пока нет проблем', noIssuesYetMessage: 'В этом сообществе пока ничего не сообщалось.',
      noPostsYetTitle: 'Пока нет постов', noPostsYetDashboardMessage: 'Станьте первым, кто напишет в ленте сообщества.',
      noEventsYetTitle: 'Нет предстоящих событий', noEventsYetMessage: 'Загляните позже - здесь появятся события сообщества.',
      noProvidersYetTitle: 'Пока нет поставщиков', noProvidersYetMessage: 'Проверенные поставщики услуг появятся здесь.',
      unableToLoadStats: 'Не удалось загрузить статистику.',
      alertNeedsAttention: 'Требуют внимания: {critical} критических и {escalated} эскалированных проблем.',
      reviewNow: 'Просмотреть', aiUnavailableShort: 'AI-помощь временно недоступна.',
      unableToLoadDashboard: 'Не удалось загрузить панель: ',

      issuesTitle: 'Проблемы', issuesSubtitle: 'Всё, о чём сообщили в вашем сообществе.',
      staleOnly: 'Только устаревшие', escalatedOnly: 'Только эскалированные',
      joinCommunityFromDashboard: 'Сначала присоединитесь к сообществу на панели управления.',
      noIssuesFoundTitle: 'Проблемы не найдены', noIssuesFoundMessage: 'Сейчас ничего не соответствует этим фильтрам.',
      unableToLoadIssues: 'Не удалось загрузить проблемы: ',
      whatHappened: 'Что случилось?', describeProblemPlaceholder: 'Опишите проблему своими словами...',
      buildingLocationOptional: 'Здание / место (необязательно)', buildingLocationPlaceholder: 'напр. Здание B',
      analyzeAndReport: 'Анализ и отправка', pleaseDescribeMore: 'Пожалуйста, опишите проблему немного подробнее.',
      aiLoadingUnderstanding: 'Анализируем ваше сообщение...', aiLoadingCategory: 'Определяем категорию проблемы...',
      aiLoadingUrgency: 'Проверяем срочность...', aiLoadingSimilar: 'Ищем похожие сообщения...',
      aiLoadingPreparing: 'Готовим ваше сообщение...',
      aiUnavailableManualFallback: 'AI-помощь временно недоступна. Вы всё ещё можете отправить проблему вручную.',
      aiUnderstoodReport: 'AI понял ваше сообщение',
      aiUsedFallbackReviewCarefully: '(AI-сервис недоступен - использован простой резервный метод на основе ключевых слов. Внимательно проверьте поля ниже.)',
      affectedGroupLabel: 'Затронутая группа', aiSummaryLabel: 'AI-сводка',
      possibleExistingIssue: 'Возможно, уже существует', residentsAffected: 'жителей затронуто',
      iHaveThisProblemToo: 'У меня тоже эта проблема', viewIssue: 'Открыть проблему',
      createNewIssueAnyway: 'Всё равно создать новую проблему', nowFollowingIssue: 'Теперь вы отслеживаете эту проблему.',
      submitIssue: 'Отправить проблему', issueReported: 'Ваша проблема отправлена.',

      backToIssues: 'Назад к проблемам', reportedBy: 'Сообщил(а)', adminActions: 'Действия администратора',
      changeStatusPlaceholder: 'Изменить статус...', updatePriority: 'Обновить приоритет',
      commentsLabel: 'Комментарии', issueTimeline: 'Хронология проблемы', noActivityYet: 'Пока нет активности.',
      youAreNowSupporting: 'Теперь вы поддерживаете эту проблему.', statusUpdated: 'Статус обновлён.',
      priorityUpdated: 'Приоритет обновлён.', unableToLoadIssueTitle: 'Не удалось загрузить эту проблему',
      noIssueSpecified: 'Проблема не указана.',

      feedSubtitle: 'Объявления, обсуждения и новости от соседей.',
      newPostBtn: 'Новый пост', allTypes: 'Все типы',
      feedEmptyMessage: 'Поделитесь первым сообщением со своим сообществом.',
      unableToLoadFeed: 'Не удалось загрузить ленту: ', pinnedLabel: 'Закреплено', unpin: 'Открепить', pin: 'Закрепить',
      failedToLoadComments: 'Не удалось загрузить комментарии.', deletePostConfirmTitle: 'Удалить пост?',
      deletePostConfirmMessage: 'Это действие нельзя отменить.', postDeleted: 'Пост удалён.',
      createAPost: 'Создать пост', announcementAssistant: 'Помощник объявлений',
      announcementAssistantHint: 'Запишите краткие заметки - AI подготовит готовый заголовок и текст, которые можно отредактировать перед публикацией.',
      aiNotesPlaceholder: 'напр. завтра с 10:00 до 14:00 отключат воду для ремонта',
      draftWithAi: 'Составить с AI', titleOptional: 'Заголовок (необязательно)', publish: 'Опубликовать',
      pleaseWriteSomething: 'Пожалуйста, сначала что-нибудь напишите.', postPublished: 'Пост опубликован.',
      typeRoughNotesFirst: 'Сначала введите несколько заметок.', drafting: 'Составление...',
      draftReadyFallback: 'Черновик готов (AI недоступен - использован простой рерайт). Проверьте перед публикацией.',
      draftReadyReview: 'Черновик готов. Просмотрите и отредактируйте перед публикацией.',
      couldNotDraftWithAi: 'Не удалось составить с AI: ',

      pollsTitle: 'Опросы', pollsSubtitle: 'Голосуйте по решениям, влияющим на ваше сообщество.',
      newPollBtn: 'Новый опрос', noPollsYetTitle: 'Пока нет опросов',
      noPollsYetMessage: 'Создайте опрос, чтобы узнать мнение сообщества.',
      unableToLoadPolls: 'Не удалось загрузить опросы: ', closedLabel: 'Закрыт', activeLabel: 'Активен',
      votesCount: 'голос(ов)', closesOn: 'Закрывается', noExpiry: 'Без срока', voteRecorded: 'Голос учтён.',
      createAPoll: 'Создать опрос', optionN: 'Вариант {n}', addOption: '+ Добавить вариант',
      expiresOptional: 'Истекает (необязательно)', createPollBtn: 'Создать опрос',
      addQuestionAndOptions: 'Добавьте вопрос и минимум 2 варианта.', pollCreated: 'Опрос создан.',

      eventsTitle: 'События', eventsSubtitle: 'Встречи, субботники и мероприятия в вашем сообществе.',
      newEventBtn: 'Новое событие', upcomingTab: 'Предстоящие', pastTab: 'Прошедшие',
      noEventsFilteredTitle: 'Нет {mode} событий', checkBackOrCreate: 'Загляните позже или создайте своё.',
      unableToLoadEvents: 'Не удалось загрузить события: ', noLocationShort: 'Без места',
      goingLabel: 'пойдут', capacitySuffix: ' / {capacity} мест', maybeLabel: 'возможно',
      statusGoing: '✓ Пойду', statusMaybe: '? Возможно', statusNotGoing: '✕ Не смогу',
      createAnEvent: 'Создать событие', locationLabel: 'Место', startLabel: 'Начало', endLabel: 'Конец',
      capacityOptional: 'Вместимость (необязательно)', createEventBtn: 'Создать событие',
      titleStartEndRequired: 'Требуются заголовок, время начала и окончания.', eventCreated: 'Событие создано.',

      lostFoundSubtitle: 'Верните потерянные вещи их владельцам.', reportItemBtn: 'Сообщить о вещи',
      allLabel: 'Все', lostLabel: 'Потеряно', foundLabel: 'Найдено', anyStatus: 'Любой статус',
      photoOptionalLabel: 'Фото (необязательно)', uploadingPhoto: 'Загрузка фото...', avatarLabel: 'Аватар',
      changePhoto: 'Изменить фото', uploadFailed: 'Не удалось загрузить фото: ',
      statusActive: 'Активно', statusClaimed: 'Востребовано', statusReturned: 'Возвращено', statusClosed: 'Закрыто',
      noItemsFoundTitle: 'Здесь пока пусто', noItemsFoundMessage: 'Нет потерянных или найденных вещей по этим фильтрам.',
      unableToLoadItems: 'Не удалось загрузить вещи: ', noPhoto: 'Нет фото', updateStatusPlaceholder: 'Изменить статус...',
      reportLostFoundTitle: 'Сообщить о потерянной или найденной вещи', lostFoundTitlePlaceholder: 'напр. Потерялся кот',
      submit: 'Отправить', pleaseAddTitle: 'Пожалуйста, добавьте заголовок.', itemReported: 'Вещь добавлена.',

      localServicesTitle: 'Местные услуги', localServicesSubtitle: 'Найдите проверенных поставщиков услуг в вашем сообществе.',
      manageProviderProfile: 'Управление профилем поставщика', browseProviders: 'Поставщики услуг',
      incomingRequests: 'Входящие заявки', myRequests: 'Мои заявки', allCategories: 'Все категории',
      verifiedOnly: 'Только проверенные', noProvidersFoundTitle: 'Поставщики не найдены',
      tryDifferentFilter: 'Попробуйте другую категорию или фильтр.', unableToLoadProviders: 'Не удалось загрузить поставщиков: ',
      verifiedBadge: '✓ Проверено', reviewsCount: 'отзывов', noRequestsYetTitle: 'Пока нет заявок',
      incomingRequestsWillAppear: 'Входящие заявки появятся здесь.',
      requestServiceToSeeHere: 'Закажите услугу на бирже, чтобы увидеть её здесь.',
      unableToLoadRequests: 'Не удалось загрузить заявки: ', accept: 'Принять', decline: 'Отклонить',
      startWork: 'Начать работу', markCompleted: 'Отметить как выполнено', leaveAReview: 'Оставить отзыв',
      cancelRequest: 'Отменить заявку', requestUpdated: 'Заявка обновлена.', ratingLabel: 'Оценка',
      commentLabel: 'Комментарий', submitReview: 'Отправить отзыв', reviewSubmittedThankYou: 'Отзыв отправлен. Спасибо!',
      updateProviderProfile: 'Обновить профиль поставщика', createProviderProfile: 'Создать профиль поставщика',
      businessNameLabel: 'Название компании', bioLabel: 'Описание', serviceAreaLabel: 'Зона обслуживания',
      categoriesLabel: 'Категории', providerProfileSaved: 'Профиль поставщика сохранён.',

      noProviderSpecified: 'Поставщик не указан.', unableToLoadProvider: 'Не удалось загрузить поставщика',
      backToServices: 'Назад к услугам', requestServiceBtn: 'Заказать услугу',
      noDescriptionProvided: 'Описание не предоставлено.', serviceAreaColon: 'Зона обслуживания:',
      notSpecified: 'Не указано', reviewsHeader: 'Отзывы', noReviewsYet: 'Пока нет отзывов.',
      requestServiceFrom: 'Заказать услугу у {name}', describeWhatYouNeed: 'Опишите, что вам нужно',
      pleaseDescribeWhatYouNeed: 'Пожалуйста, опишите, что вам нужно.', sendRequest: 'Отправить заявку',
      serviceRequestSent: 'Заявка на услугу отправлена.',

      notificationsSubtitle: 'Всё, что требует вашего внимания.', markAllRead: 'Отметить все как прочитанные',
      noNotificationsTitle: 'Нет уведомлений', allCaughtUp: 'Вы всё просмотрели.',
      markAsRead: 'Отметить как прочитанное', unableToLoadNotifications: 'Не удалось загрузить уведомления: ',
      allNotificationsMarkedRead: 'Все уведомления отмечены как прочитанные.',

      adminSectionLabel: 'Админ', operationsCenter: 'Операционный центр',
      navHome: 'Главная', navReport: 'Сообщить', navCommunity: 'Сообщество',
      dropdownProfile: 'Профиль', dropdownNotifications: 'Уведомления', dropdownLogout: 'Выйти',
      loadingCommunities: 'Загрузка...', noCommunitiesYet: 'Пока нет сообществ',
      searchPlaceholder: 'Поиск проблем, постов, поставщиков...',

      manageAccountDetails: 'Управляйте данными своего аккаунта.',
      personalInformation: 'Личная информация', avatarUrlOptional: 'URL аватара (необязательно)',
      preferredLanguageLabel: 'Предпочитаемый язык', saveChanges: 'Сохранить изменения',
      accountLabel: 'Аккаунт', roleLabel: 'Роль', changePasswordHeader: 'Изменить пароль',
      currentPasswordLabel: 'Текущий пароль', newPasswordLabel: 'Новый пароль',
      updatePasswordBtn: 'Обновить пароль', profileUpdated: 'Профиль обновлён.', passwordUpdated: 'Пароль обновлён.',

      signInSubtitle: 'Войдите, чтобы продолжить работу с сообществом.',
      sessionExpired: 'Срок действия сессии истёк. Пожалуйста, войдите снова.',
      showPassword: 'Показать', hidePassword: 'Скрыть',
      exploreDemo: '✨ Изучить демо', noAccountYet: 'Нет аккаунта?',
      alreadyHaveAccount: 'Уже есть аккаунт?',
      exploreDemoTitle: 'Изучите Yaxın.az как демо-пользователь',
      exploreDemoIntro: 'Перейдите прямо в приложение без регистрации. Выберите роль, чтобы увидеть платформу с этой точки зрения.',
      demoResidentDesc: 'Сообщайте о проблемах, участвуйте в ленте, голосуйте в опросах',
      demoCommunityAdminDesc: 'Управляйте проблемами, участниками и объявлениями',
      demoServiceProviderDesc: 'Просматривайте заявки на услуги и отвечайте на них',
      demoPlatformAdminDesc: 'Операционный центр, журнал аудита, модерация',
      roleResident: 'Житель', roleCommunityAdmin: 'Администратор сообщества',
      roleServiceProvider: 'Поставщик услуг', rolePlatformAdmin: 'Администратор платформы',
      demoLoginFailed: 'Не удалось выполнить демо-вход: ',
      joinYourCommunitySubtitle: 'Создайте аккаунт Yaxın.az - это займёт меньше минуты.',
      atLeast8Chars: 'Минимум 8 символов.', welcomeToApp: 'Добро пожаловать в Yaxın.az!',

      adminAccessRequired: 'Требуется доступ администратора.', adminDashboardTitle: 'Панель администратора',
      adminDashboardSubtitle: 'Состояние сообщества с высоты птичьего полёта.',
      selectCommunityFirst: 'Сначала выберите или создайте сообщество.',
      pleaseTryAgainLater: 'Пожалуйста, попробуйте позже.',
      criticalIssuesLabel: 'Критические проблемы', staleIssuesLabel: 'Устаревшие проблемы', escalatedLabel: 'Эскалировано', staleLabel: 'Устарело',
      resolutionRateLabel: 'Процент решения', avgResolutionHrsLabel: 'Среднее время решения (ч)',
      providersPlatformLabel: 'Поставщики (платформа)', issuesByCategory: 'Проблемы по категориям',
      issuesByPriority: 'Проблемы по приоритету', activityTrend7Day: 'Тренд активности за 7 дней',
      aiCommunityInsights: 'AI-аналитика сообщества', notEnoughDataInsights: 'Пока недостаточно данных для содержательной аналитики.',
      pendingMemberships: 'Заявки на членство', pendingProvidersLabel: 'Ожидающие поставщики', noPendingRequests: 'Нет ожидающих заявок.',
      approve: 'Одобрить', reject: 'Отклонить', statusApprovedWord: 'одобрено', statusRejectedWord: 'отклонено',
      chartsUnavailable: 'Графики временно недоступны.',
      membershipStatusUpdated: 'Членство: {status}.', pendingProviderVerification: 'Ожидают проверки',
      noPendingProviders: 'Нет ожидающих поставщиков.', verify: 'Проверить', moderationQueue: 'Очередь модерации',
      noPendingReports: 'Нет ожидающих жалоб.', review: 'Просмотр', providerVerified: 'Поставщик проверен.',
      unableToLoadAdminWidgets: 'Не удалось загрузить виджеты администратора.',
      operationsCenterSubtitle: 'Состояние платформы и критические события в реальном времени.', refresh: '↻ Обновить',
      escalatedIssuesLabel: 'Эскалированные проблемы', moderationQueueLabel: 'Очередь модерации',
      systemHealth: 'Состояние системы', applicationLabel: 'Приложение', databaseLabel: 'База данных',
      aiProviderLabel: 'AI-провайдер', webSocketLabel: 'WebSocket', activeProfileLabel: 'Активный профиль',
      lastChecked: 'Последняя проверка', reviewFromAdminDashboard: 'Просмотр из панели администратора каждого сообщества.',
      reviewFromProviderWidget: 'Просмотр из виджета поставщиков панели администратора.',
      moderationReportsLabel: 'Жалобы на модерацию', takeAction: 'Принять меры', dismiss: 'Отклонить',
      recentAuditTrail: 'Журнал аудита', noAuditEntriesYet: 'Пока нет записей аудита.',
      reportUpdated: 'Жалоба обновлена.', platformAdminAccessRequired: 'Требуется доступ администратора платформы',
      operationsCenterPlatformOnly: 'Операционный центр доступен только администраторам платформы.',
      unableToLoadOperationsCenter: 'Не удалось загрузить операционный центр',

      landingSignIn: 'Войти', landingJoinCommunity: 'Присоединиться к сообществу',
      landingBadge: '✨ Умная платформа сообщества на базе AI',
      landingHeroTitle: 'Ваше сообщество.<br/>Умнее. Ближе. Связаннее.',
      landingHeroLead: 'Сообщайте о проблемах, общайтесь с соседями, находите проверенные местные услуги, и пусть AI поможет вашему сообществу работать умнее.',
      landingJoinCta: 'Присоединиться к сообществу', landingExploreDemo: 'Изучить демо',
      landingWhySection: 'Почему Yaxın.az', landingWhyTitle: 'Всё, что нужно вашему сообществу, в одном месте',
      landingFeature1Title: 'Умное сообщение о проблемах',
      landingFeature1Desc: 'Опишите проблему простым языком - AI поймёт её, классифицирует и мгновенно проверит на дубликаты.',
      landingFeature2Title: 'Понимание на основе AI',
      landingFeature2Desc: 'Claude анализирует сообщения по категории, срочности и затронутым жителям - Java решает, что делать дальше.',
      landingFeature3Title: 'Лента сообщества',
      landingFeature3Desc: 'Объявления, оповещения и обсуждения - всё в одном месте, посты администраторов чётко выделены.',
      landingFeature4Title: 'Проверенные местные услуги',
      landingFeature4Desc: 'Найдите проверенных, оценённых сантехников, электриков, клинеров и многое другое - прямо в вашем сообществе.',
      landingFeature5Title: 'События и опросы',
      landingFeature5Desc: 'Организуйте встречи, субботники и решения сообщества с голосованием и учётом посещаемости в реальном времени.',
      landingFeature6Title: 'Уведомления в реальном времени',
      landingFeature6Desc: 'Получайте уведомления в момент изменения статуса вашей проблемы или публикации важного объявления.',
      landingFeature7Title: 'Аналитика сообщества',
      landingFeature7Desc: 'Администраторы видят реальные показатели решения проблем, тренды и AI-аналитику - никаких догадок.',
      landingFeature8Title: 'Управление операциями',
      landingFeature8Desc: 'Операционный центр корпоративного уровня для критических проблем, эскалаций и состояния системы.',
      landingCtaTitle: 'Готовы перевести своё сообщество онлайн?',
      landingCtaLead: 'Присоединяйтесь за минуты или изучите полностью рабочее демо с реальными данными.',
      landingFooter: 'Yaxın.az · Итоговый учебный проект Java/Spring ·',
      landingApiDocs: 'Документация API',

      searchTitle: 'Поиск', searchSubtitle: 'Поиск по проблемам, постам, событиям, бюро находок и поставщикам услуг.',
      smartSearchBtn: 'Умный поиск', smartSearchTitleAttr: 'Позвольте AI понять, что вы имеете в виду, и в каких разделах искать',
      sectionCommunityPosts: 'Посты сообщества', sectionServiceProviders: 'Поставщики услуг',
      startTypingToSearch: 'Начните вводить текст для поиска', enterAtLeast2Chars: 'Введите минимум 2 символа.',
      smartSearchUnderstood: 'Умный поиск понял ваш запрос', noResultsFoundTitle: 'Результаты не найдены',
      nothingMatched: 'Ничего не найдено по запросу "{q}".', searchFailed: 'Ошибка поиска: ', smartSearchFailed: 'Ошибка умного поиска: ',
      searchEmptyStateMessage: 'Ищите проблемы, посты, события и многое другое - или нажмите Умный поиск, чтобы спросить простым языком.',
      aiUnavailableFallbackUsed: '(AI-сервис недоступен - использован простой резервный метод на основе ключевых слов.)',

      loginVisualHeadline: 'Ближе сообщества.<br/>Умнее жизнь.',
      loginVisualLead: 'Сообщайте о проблемах, общайтесь с соседями, находите проверенные местные услуги, и пусть AI поможет вашему сообществу работать умнее.',
      loginVisualPoint1: '✓ AI мгновенно понимает ваши сообщения',
      loginVisualPoint2: '✓ Обновления в реальном времени от администраторов сообщества',
      loginVisualPoint3: '✓ Проверенные, оценённые местные поставщики услуг',
      registerVisualHeadline: 'Ваше сообщество - в одной платформе.',
      registerVisualLead: 'Создайте аккаунт, присоединитесь к своему дому и начните участвовать - сообщайте о проблемах, участвуйте в обсуждениях и находите проверенную местную помощь.',

      errUnableToReachServer: 'Не удалось подключиться к серверу. Проверьте соединение.',
      errForbidden: 'У вас нет прав для выполнения этого действия.',
      errNotFound: 'Запрашиваемый элемент не найден.',
      errConflict: 'Эта запись была обновлена другим пользователем. Обновите страницу и попробуйте снова.',
      errRateLimited: 'Вы делаете это слишком часто. Подождите немного и попробуйте снова.',
      errServerError: 'Произошла ошибка на нашей стороне. Повторите попытку позже.',
      errGeneric: 'Не удалось выполнить запрос.',
      errInvalidCredentials: 'Неверный email или пароль.',
      validationFailed: 'Ошибка валидации',
      valRequired: 'Это поле обязательно.',
      valEmailInvalid: 'Введите корректный адрес электронной почты.',
      valMaxLength: 'Не более {max} символов.',
      valSizeRange: 'Должно быть от {min} до {max} символов.',
      valMinValue: 'Должно быть не менее {min}.',
      valMaxValue: 'Должно быть не более {max}.',
      valPositive: 'Должно быть положительным числом.',

      'enum.PostType.GENERAL': 'Общее', 'enum.PostType.ANNOUNCEMENT': 'Объявление',
      'enum.PostType.EVENT': 'Событие', 'enum.PostType.QUESTION': 'Вопрос',
      'enum.PostType.ALERT': 'Предупреждение', 'enum.PostType.MARKETPLACE': 'Барахолка',

      'enum.IssueStatus.OPEN': 'Открыто', 'enum.IssueStatus.ACKNOWLEDGED': 'Принято',
      'enum.IssueStatus.IN_PROGRESS': 'В процессе', 'enum.IssueStatus.WAITING_FOR_VENDOR': 'Ожидание подрядчика',
      'enum.IssueStatus.RESOLVED': 'Решено', 'enum.IssueStatus.CLOSED': 'Закрыто',
      'enum.IssueStatus.REJECTED': 'Отклонено',

      'enum.IssuePriority.LOW': 'Низкий', 'enum.IssuePriority.MEDIUM': 'Средний',
      'enum.IssuePriority.HIGH': 'Высокий', 'enum.IssuePriority.CRITICAL': 'Критический',

      'enum.IssueCategory.ELEVATOR': 'Лифт', 'enum.IssueCategory.WATER': 'Вода',
      'enum.IssueCategory.ELECTRICITY': 'Электричество', 'enum.IssueCategory.GAS': 'Газ',
      'enum.IssueCategory.HEATING': 'Отопление', 'enum.IssueCategory.PARKING': 'Парковка',
      'enum.IssueCategory.SECURITY': 'Безопасность', 'enum.IssueCategory.NOISE': 'Шум',
      'enum.IssueCategory.CLEANING': 'Уборка', 'enum.IssueCategory.WASTE': 'Отходы',
      'enum.IssueCategory.INTERNET': 'Интернет', 'enum.IssueCategory.BUILDING_DAMAGE': 'Повреждение здания',
      'enum.IssueCategory.ROAD': 'Дорога', 'enum.IssueCategory.LIGHTING': 'Освещение',
      'enum.IssueCategory.ANIMAL': 'Животные', 'enum.IssueCategory.ACCESSIBILITY': 'Доступность',
      'enum.IssueCategory.OTHER': 'Другое',

      'enum.AttendanceStatus.GOING': 'Иду', 'enum.AttendanceStatus.MAYBE': 'Возможно',
      'enum.AttendanceStatus.NOT_GOING': 'Не иду',

      'enum.LostFoundType.LOST': 'Потеряно', 'enum.LostFoundType.FOUND': 'Найдено',

      'enum.LostFoundStatus.ACTIVE': 'Активно', 'enum.LostFoundStatus.CLAIMED': 'Востребовано',
      'enum.LostFoundStatus.RETURNED': 'Возвращено', 'enum.LostFoundStatus.CLOSED': 'Закрыто',

      'enum.ServiceRequestStatus.REQUESTED': 'Запрошено', 'enum.ServiceRequestStatus.ACCEPTED': 'Принято',
      'enum.ServiceRequestStatus.DECLINED': 'Отклонено', 'enum.ServiceRequestStatus.IN_PROGRESS': 'В процессе',
      'enum.ServiceRequestStatus.COMPLETED': 'Завершено', 'enum.ServiceRequestStatus.CANCELLED': 'Отменено',

      'enum.ServiceCategory.PLUMBING': 'Сантехника', 'enum.ServiceCategory.ELECTRICAL': 'Электрика',
      'enum.ServiceCategory.CLEANING': 'Уборка', 'enum.ServiceCategory.HANDYMAN': 'Мастер на час',
      'enum.ServiceCategory.LOCKSMITH': 'Слесарь', 'enum.ServiceCategory.MOVING': 'Переезд',
      'enum.ServiceCategory.APPLIANCE_REPAIR': 'Ремонт техники', 'enum.ServiceCategory.PET_CARE': 'Уход за животными',
      'enum.ServiceCategory.TUTORING': 'Репетиторство', 'enum.ServiceCategory.CAR_SERVICE': 'Автосервис',
      'enum.ServiceCategory.OTHER': 'Другое',

      'enum.MembershipStatus.PENDING': 'В ожидании', 'enum.MembershipStatus.APPROVED': 'Одобрено',
      'enum.MembershipStatus.REJECTED': 'Отклонено', 'enum.MembershipStatus.BLOCKED': 'Заблокировано',

      'enum.Role.RESIDENT': 'Житель', 'enum.Role.COMMUNITY_ADMIN': 'Администратор сообщества',
      'enum.Role.SERVICE_PROVIDER': 'Поставщик услуг', 'enum.Role.PLATFORM_ADMIN': 'Администратор платформы',

      'enum.CommunityType.APARTMENT_BUILDING': 'Многоквартирный дом',
      'enum.CommunityType.RESIDENTIAL_COMPLEX': 'Жилой комплекс',
      'enum.CommunityType.NEIGHBORHOOD': 'Район',
      'enum.CommunityType.STUDENT_RESIDENCE': 'Студенческое общежитие',
      'enum.CommunityType.PRIVATE_COMMUNITY': 'Частное сообщество',
      'enum.CommunityType.OTHER': 'Другое',

      'enum.ContentType.POST': 'Пост', 'enum.ContentType.COMMENT': 'Комментарий',
      'enum.ContentType.ISSUE': 'Проблема', 'enum.ContentType.LOST_FOUND_ITEM': 'Находка/пропажа',
      'enum.ContentType.REVIEW': 'Отзыв',

      'enum.ReportReason.SPAM': 'Спам', 'enum.ReportReason.ABUSE': 'Злоупотребление',
      'enum.ReportReason.MISINFORMATION': 'Дезинформация',
      'enum.ReportReason.INAPPROPRIATE_CONTENT': 'Неприемлемый контент',
      'enum.ReportReason.OTHER': 'Другое',
    },
    TR: {
      dashboard: 'Kontrol Paneli', community: 'Topluluk', issues: 'Sorunlar', feed: 'Akış',
      polls: 'Anketler', events: 'Etkinlikler', lostFound: 'Kayıp ve Bulunan', services: 'Hizmetler',
      notifications: 'Bildirimler', profile: 'Profil', admin: 'Yönetim', search: 'Arama',
      reportProblem: 'Sorun Bildir', logout: 'Çıkış yap', signIn: 'Giriş yap', signUp: 'Kayıt ol',
      welcomeBack: 'Tekrar hoş geldiniz', joinYourCommunity: 'Topluluğunuza katılın',
      email: 'E-posta', password: 'Şifre', firstName: 'Ad', lastName: 'Soyad',
      loading: 'Yükleniyor...', save: 'Kaydet', cancel: 'İptal', delete: 'Sil', confirm: 'Onayla',
      noResults: 'Burada henüz bir şey yok.', apply: 'Uygula', close: 'Kapat',
    },
  };

  function current() {
    return localStorage.getItem(window.YAXINAZ_CONFIG.langStorageKey) || 'EN';
  }

  function setLanguage(lang) {
    localStorage.setItem(window.YAXINAZ_CONFIG.langStorageKey, lang);
    location.reload();
  }

  /** t('key', {name: 'value'}) substitutes {name} placeholders in the translated string. */
  function t(key, params) {
    const lang = current();
    let str = (dict[lang] && dict[lang][key]) || dict.EN[key] || key;
    if (params) {
      Object.entries(params).forEach(([k, v]) => { str = str.replace(`{${k}}`, v); });
    }
    return str;
  }

  /**
   * Translates every element with data-i18n="key" found on the page. Uses innerHTML, not
   * textContent, because a few dictionary values (e.g. landingHeroTitle) contain a literal <br/>
   * for a deliberate line break - textContent would render that tag as visible text instead of
   * breaking the line. Safe here because every dict value is a static string this file's authors
   * wrote themselves, never user input.
   */
  function applyToDom(root = document) {
    root.querySelectorAll('[data-i18n]').forEach((el) => {
      el.innerHTML = t(el.getAttribute('data-i18n'));
    });
  }

  /**
   * Bean Validation (Hibernate Validator) field-error messages come from the backend in their
   * default English wording (e.g. "must not be blank", "size must be between 8 and 100") - the
   * backend has no locale support, so these never respected the UI language switcher. This maps
   * the finite, known set of default messages this app's DTOs actually trigger (@NotBlank,
   * @NotNull, @Email, @Size, @Min, @Max, @Positive) to a translated equivalent. Any message that
   * doesn't match a known pattern (e.g. a future constraint type) falls back to the raw backend
   * text unchanged, rather than showing nothing.
   */
  function translateFieldError(rawMessage) {
    if (!rawMessage) return rawMessage;
    if (/^must not be (blank|null|empty)$/.test(rawMessage)) return t('valRequired');
    if (/^must be a well-formed email address$/.test(rawMessage)) return t('valEmailInvalid');
    let m = rawMessage.match(/^size must be between (\d+) and (\d+)$/);
    if (m) {
      const min = Number(m[1]), max = Number(m[2]);
      return min === 0 ? t('valMaxLength', { max }) : t('valSizeRange', { min, max });
    }
    m = rawMessage.match(/^must be greater than or equal to (\d+)$/);
    if (m) return t('valMinValue', { min: m[1] });
    m = rawMessage.match(/^must be less than or equal to (\d+)$/);
    if (m) return t('valMaxValue', { max: m[1] });
    if (/^must be greater than 0$/.test(rawMessage)) return t('valPositive');
    return rawMessage;
  }

  /** Maps api.js's client-authored fallback error strings to the translated equivalent. */
  function translateApiMessage(rawMessage) {
    const known = {
      'Unable to reach the server. Please check your connection.': 'errUnableToReachServer',
      'Your session has expired. Please sign in again.': 'sessionExpired',
      'You do not have permission to perform this action.': 'errForbidden',
      'The requested item could not be found.': 'errNotFound',
      'This record was updated by another user. Please refresh and try again.': 'errConflict',
      'You are doing that too often. Please wait a moment and try again.': 'errRateLimited',
      'Something went wrong on our end. Please try again shortly.': 'errServerError',
      'The request could not be completed.': 'errGeneric',
      'Validation failed': 'validationFailed',
      'Invalid email or password.': 'errInvalidCredentials',
    };
    const key = known[rawMessage];
    return key ? t(key) : rawMessage;
  }

  /**
   * Translates a backend enum value (IssueStatus.OPEN, PostType.ANNOUNCEMENT, etc.) for display.
   * `group` is the enum's simple Java type name, `value` the raw enum constant. Falls back to a
   * prettified (title-cased, underscore-to-space) version of the raw value when no translation
   * exists yet for that group/value pair, so an unlisted enum constant still degrades gracefully
   * instead of showing a raw i18n key or being dropped.
   */
  function enumLabel(group, value) {
    if (!value) return value;
    const key = `enum.${group}.${value}`;
    const lang = current();
    const found = (dict[lang] && dict[lang][key]) || (dict.EN && dict.EN[key]);
    if (found) return found;
    return String(value).split('_').map((w) => w.charAt(0) + w.slice(1).toLowerCase()).join(' ');
  }

  // Manual AZ month/weekday tables - some Chromium builds (including the one Playwright bundles,
  // and reportedly some real-world Chrome installs with a reduced ICU dataset) report the "az"
  // locale as "supported" via Intl.DateTimeFormat.supportedLocalesOf, but actually have no CLDR
  // month/weekday name data for it and silently fall back to a raw "M09"-style token instead of
  // "sen" - caught by screenshotting events.html in AZ, not by any functional check (no exception
  // is thrown, the string just looks wrong). EN/RU are unaffected (verified directly), so only AZ
  // gets the manual table; everything else still goes through the native Intl API.
  const AZ_MONTHS_SHORT = ['yan', 'fev', 'mar', 'apr', 'may', 'iyn', 'iyl', 'avq', 'sen', 'okt', 'noy', 'dek'];
  const AZ_WEEKDAYS_SHORT = ['B.', 'B.e.', 'Ç.a.', 'Ç.', 'C.a.', 'C.', 'Ş.'];

  function shortMonth(date) {
    if (current() === 'AZ') return AZ_MONTHS_SHORT[date.getMonth()];
    return date.toLocaleString(current().toLowerCase(), { month: 'short' });
  }

  function shortWeekdayTime(date) {
    const time = date.toLocaleString('en-GB', { hour: '2-digit', minute: '2-digit', hour12: false });
    if (current() === 'AZ') return `${AZ_WEEKDAYS_SHORT[date.getDay()]} ${time}`;
    return `${date.toLocaleString(current().toLowerCase(), { weekday: 'short' })} ${time}`;
  }

  return { t, current, setLanguage, applyToDom, translateFieldError, translateApiMessage, enumLabel, shortMonth, shortWeekdayTime };
})();
