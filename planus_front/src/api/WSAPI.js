import SockJS from "sockjs-client";
import Stomp from "webstomp-client";

const WS_SERVER_URL = process.env.VUE_APP_API_URL + "/ws";

const WSAPI = {
  socket: null,
  stomp: null,
  connect(tripId, token, callback) {
    this.socket = new SockJS(WS_SERVER_URL);
    this.stomp = Stomp.over(this.socket);
    this.stomp.debug = () => {
      return;
    };
    this.stomp.connect(
      {},
      () => {
        this.stomp.subscribe("/topic/planus/" + tripId, callback);
        this.enter({ tripId, token });
      },
      (error) => {
        console.log(error);
        this.connect = false;
      }
    );
  },
  enter(member) {
    this.stomp.send("/app/enter", JSON.stringify(member));
  },
  getConnector(member) {
    this.stomp.send("/app/connector", JSON.stringify(member));
  },
  chat(message) {
    this.stomp.send("/app/chat", JSON.stringify(message));
  },
  addBucket(tripId, place, address, lat, lng) {
    let bucket = {
      tripId: tripId,
      place: place,
      address: address,
      lat: lat,
      lng: lng,
    };
    this.stomp.send("/app/addBucket", JSON.stringify(bucket));
  },
  delBucket(tripId, place, address, lat, lng) {
    let bucket = {
      tripId: tripId,
      place: place,
      address: address,
      lat: lat,
      lng: lng,
    };
    this.stomp.send("/app/delBucket", JSON.stringify(bucket));
  },
  setPlan(tripId, planId, tripDate, startTime) {
    let plan = {
      tripId: tripId,
      planId: planId,
      tripDate: tripDate,
      startTime: startTime,
    };
    this.stomp.send("/app/setPlan", JSON.stringify(plan));
  },

  addTimetable(
    tripId,
    planId,
    costTime,
    place,
    lat,
    lng,
    orders,
    fromBucket,
    address
  ) {
    let timetable = {
      tripId: tripId,
      planId: planId,
      costTime: costTime,
      place: place,
      lat: lat,
      lng: lng,
      orders: orders,
      fromBucket: fromBucket,
      address: address,

      transit: "NONE",
      moveTime: 0,
      moveRoute: "",
    };
    this.stomp.send("/app/addTimetable", JSON.stringify(timetable));
  },
  delTimetable(tripId, planId, timetableList) {
    let delTimetableList = {
      tripId: tripId,
      planId: planId,
      timetableList: timetableList,
    };
    this.stomp.send("/app/delTimetable", JSON.stringify(delTimetableList));
  },
  setTimetableOrders(tripId, planId, timetableList) {
    let setTimetableOrdersList = {
      tripId: tripId,
      planId: planId,
      timetableList: timetableList,
    };
    this.stomp.send(
      "/app/setTimetableOrders",
      JSON.stringify(setTimetableOrdersList)
    );
  },
  setTimetable(
    tripId,
    planId,
    place,
    lat,
    lng,
    orders,
    costTime,
    moveTime,
    transit,
    moveRoute,
    fromBucket
  ) {
    let timetable = {
      tripId: tripId,
      planId: planId,
      place: place,
      lat: lat,
      lng: lng,
      orders: orders,
      costTime: costTime,
      moveTime: moveTime,
      transit: transit,
      moveRoute: moveRoute,
      fromBucket: fromBucket,
    };
    this.stomp.send("/app/setTimetable", JSON.stringify(timetable));
  },

  changeAdmin(tripId, newAdminId, originAdminId) {
    let admin = {
      tripId: tripId,
      newAdminId: newAdminId,
      originAdminId: originAdminId,
    };
    this.stomp.send("/app/changeAdmin", JSON.stringify(admin));
  },
  completeTrip(tripId, completed) {
    let complete = {
      tripId: tripId,
      completed: completed,
    };
    this.stomp.send("/app/completeTrip", JSON.stringify(complete));
  },
};

export default WSAPI;
