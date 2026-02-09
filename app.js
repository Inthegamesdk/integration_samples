(async () => {

	/*
 	*
 	* First connect itg script in index.html file
 	* <script defer id="inthegame-script" src="https://html5.inthegame.io/v2-3/main.js"></script>
 	*
	*/

	const ITG_ACCOUNT_ID = '67ceb8673be30f2a5c3e5574';
	const ITG_CHANNEL_SLUG = 'test-channel';

	function initITG() {
		const config = {
			videoPlayerId: 'video',
			accountId: ITG_ACCOUNT_ID,
			channelSlug: ITG_CHANNEL_SLUG,
		};

		window.inthegame.init(config);
	}


	/*
	 *
	 * example datazoom integration depends on your project
	 *
	 */

	const mediatailor = datazoom.mediatailor;
	let player, datazoomContext;

	datazoom.init({
		configuration_id: "f5562a7c-9e5f-4c60-b7c4-d174808c5d38",
		demo: "true"
	});

	const sessionConfig = {
		sessionInitUrl: 'SESSION_ENDPOINT',
		incomingAdCountdownInterval: 10,
		defaultTrackingRequestInterval: 1,
		palNonceRequestParams: {
			adWillAutoPlay: true,
			adWillPlayMuted: true,
			continuousPlayback: false,
			descriptionUrl: "https://example.com",
			iconsSupported: true,
			playerType: "Sample Player Type",
			playerVersion: "1.0",
			ppid: "12JD92JD8078S8J29SDOAKC0EF230337",
			url: "https://developers.google.com/ad-manager/pal/html5",
			videoHeight: 720,
			videoWidth: 1280,
			omidPartnerName: datazoom.omidPartnerName,
			omidPartnerVersion: datazoom.omidPartnerVersion
		},
	};

	mediatailor.setLogLevel(mediatailor.LogLevel.DEBUG);

	mediatailor.initPal(null, {
		allowStorage: false
	});

	mediatailor.createSession(sessionConfig).then(
		session => {
			player = videojs("video", {}, () => {
				// Datazoom SDK initialization
				datazoomContext = datazoom.createContext(player);
				datazoomContext.attachMediaTailorSession(session);
				// Start OM Session Client
				datazoomContext.startOmidSessionClient({
					omidServiceWindow: window.self,
					videoElement: document.getElementById("video"),
					contentURL: location.href,
					accessModeHandler: (url, vendor, params) => "full"
				});
				// Start playback
				player.src({src: session.getPlaybackUrl(), type: "application/x-mpegURL"});

				initDataTrace(datazoomContext, // for demo only
					document.getElementById("dz_data"));
			});

			player.on("click", event => {
				if (event.target.tagName === "VIDEO") {
					// session.onVideoClick(event);
				} else {
					session.onPlayerControlClick(event);
				}
			});

			/*
			 *
			 * when session is created and player is ready init ITG SDK
			 *
			 */

			player.on("ready", initITG);

			// Handle MediaTailor session events
			session.addEventListener(mediatailor.SessionUiEvent.AD_START, event => {
				console.log("AD_START: " + event.detail.adElapsedTime, event.detail);
			});
			session.addEventListener(mediatailor.SessionUiEvent.AD_END, event => {
				console.log("AD_END", event.detail);
			});
			session.addEventListener(mediatailor.SessionUiEvent.AD_PROGRESS, event => {
				console.log("AD_PROGRESS: " + event.detail.adElapsedTime, event.detail);
			});
			session.addEventListener(mediatailor.SessionUiEvent.AD_CLICK, event => {
				console.log("AD_CLICK", event.detail);
				if (event.detail.adClickthroughUrl) {
					window.open(event.detail.adClickthroughUrl, "_blank");
					session.onAdClickthrough();
				}
			});
			session.addEventListener(mediatailor.SessionUiEvent.AD_CAN_SKIP, event => {
				console.log("AD_CAN_SKIP", event.detail);
			});

			/*
			 *
			 * Example of processing non linear ad
			 *
			 */

			session.addEventListener(mediatailor.SessionUiEvent.NONLINEAR_AD_START, event => {
				console.log("NONLINEAR_AD_START: " + event.detail.adElapsedTime, event.detail);

				const ad = event.detail.nonLinearAdsObject.adData.nonLinearAdList[0];
				const flexiUrl = ad.staticResource
				window.inthegame.injectFlexi(flexiUrl);
			});

			session.addEventListener(mediatailor.SessionUiEvent.NONLINEAR_AD_END, event => {
				console.log("NONLINEAR_AD_END: " + event.detail.adElapsedTime, event.detail);

				window.inthegame.closeFlexi();
			});
		},
		error => {
			console.error(`Session initialization error`, error);
		}
	);
})();
