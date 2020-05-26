import { NativeModules, DeviceEventEmitter } from 'react-native';

const { RNValidicMobileSHealth } = NativeModules;

/**
 * @namespace
 * @description
 * 
 * The Validic Mobile library provides a simple way to read and upload data from S Health to Validic. Through `ValidicSHealth`
 * you can subscribe to specific SHealth data types and automatically upload them to Validic in the background as new data is 
 * recorded.
 */
var ValidicSHealth = {
    /**
     * @typedef {Object|Boolean} ForegroundConfig
     * @memberof ValidicSHealth
     * @property {Number} notification_id - Custom ID to assign for the notification
     * @property {String} notification_title - Title to be displayed in the notification
     * @property {String} notification_message - Message to be displayed in the notification
     * @property {String} notification_icon - Resource name for icon to be displayed. e.g ic_launcher.png
     * @property {String} notification_clear_text - Text to be displayed in button to stop Samsung Health Service.
     *                                              if undefined or null, no button will be created
     */

    /**
     * @static
     * Configures the module for foreground service use. This must be called every app startup.
     * @param {ForegroundConfig} config
     */
    configure(config) {
        RNValidicMobileSHealth.configure(config);
    },

    /**
     * Represents the SHealth HealthConstants string representation.
     * @enum {string}
     * @readonly
     */
    HealthDataType: {
        DAILY_STEP_COUNT: "com.samsung.shealth.step_daily_trend",
        BLOOD_PRESSURE: "com.samsung.health.blood_pressure",
        TEMPERATURE: "com.samsung.health.body_temperature",
        HEART_RATE: "com.samsung.health.heart_rate",
        SPO2: "com.samsung.health.oxygen_saturation",
        GLUCOSE: "com.samsung.health.blood_glucose",
        HBA1C: "com.samsung.health.hba1c",
        WEIGHT: "com.samsung.health.weight",
        FOOD_INFO: "com.samsung.health.food_info",
        FOOD_INTAKE: "com.samsung.health.food_intake",
        WATER_INTAKE: "com.samsung.health.water_intake",
        CAFFEINE_INTAKE: "com.samsung.health.caffeine_intake",
        EXERCISE: "com.samsung.health.exercise",
        SLEEP: "com.samsung.health.sleep",
        SLEEP_STAGE: "com.samsung.health.sleep_stage",
        NUTRITION: "com.samsung.health.nutrition"
    },


    /**
     * @static
     * Add Subscriptions to be observed by the Validic Mobile Samsung Health SDK
     * @param {HealthDataType[]} subscriptions  - Array of Health Data Types to add observation for 
     */
    addSubscriptions(subscriptions) {
        RNValidicMobileSHealth.addSubscriptions(subscriptions);
    },

    /**
     * @static
     * Remove subscriptions to Samsung Health data types
     * @param {Array<String>} subscriptions  - Array of Health Data Types to remove observation for
     */
    removeSubscriptions(subscriptions) {
        RNValidicMobileSHealth.removeSubscriptions(subscriptions);
    },

    /**
     * @callback SubscriptionsCallback
     * @memberof ValidicSHealth
     * @param {HealthDataType} subscriptions  - Array of Health Data Types currently subscribed to
     */

    /**
     * @static
     * return a list of currently subscribed Health Data Types from Samsung Health
     * @param {SubscriptionsCallback} 
     * @param callback - callback that includes the currently subscribed HealthDataTypes
     */
    getCurrentSubscriptions(callback) {
        RNValidicMobileSHealth.getCurrentSubscriptions(callback);
    },
    /**
     * Fetch history from a Historical Set.
     * Because this processes a large amount of data, it is recommended not call this frequently. 
     * @param {Array<HistoricalSetType>} set of types to read User history from SHealth from the last 6 months. 
     */
    fetchHistory(set) {
        RNValidicMobileSHealth.fetchHistory(set);
    },

    /**
    * Events emitted by the native modules can be subscribed to here. Listeners can be registered to at any 
    * place in the app but should be unregistered when no longer needed
    */
    eventEmitter: DeviceEventEmitter,

    /**
     * Validic Samsung Health SDK Event names
     * @enum
     * @readonly
     */
    EventNames: {
        EVENT_ON_RECORDS: "validic:shealth:onrecords",
        EVENT_ON_ERROR: "validic:shealth:onerror",
        EVENT_ON_PERMISSION_CHANGE: "validic:shealth:onpermissionchange",
        EVENT_ON_HISTORY_FETCH: "validic:shealth:onhistoryfetch"
    },


    /**
     * Available types for fetching user historical data from shealth
     * @enum
     * @readonly
     */
    HistoricalSetType: {
        HistoricalSetTypeRoutine: "ROUTINE",
        HistoricalSetTypeFitness: "FITNESS"
    },

    /**
     * @enum {String}
     * Summary keys for objects returned by fetching history and subscriptions
     */
    SummaryType: {
        SummaryTypeRoutine: "routine",
        SummaryTypeBiometrics: "biometrics",
        SummaryTypeDiabetes: "diabetes",
        SummaryTypeNutrition: "nutrition",
        SummaryTypeSleep: "sleep",
        SummaryTypeFitness: "fitness",
        SummaryTypeWeight: "weight"

    }
}

export default ValidicSHealth;


