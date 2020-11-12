import {
	SAVE_STOMP_CLIENT,
} from "./mutations-types";

export default {
	[SAVE_STOMP_CLIENT](state, stompClient) {
		state.stompClient = stompClient
	},
}