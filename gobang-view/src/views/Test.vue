<template>
	<div>
		<el-button type="primary" @click="connect">connect</el-button>
		<br>
		userSendServer
		<el-input v-model="serverMsg"></el-input>
		<el-button type="primary" size="medium" @click="userSendServer">userSendServer</el-button>
		<br>
		userSendAllUser
		<el-input v-model="allMsg"></el-input>
		<el-button type="primary" size="medium" @click="userSendAllUser">userSendAllUser</el-button>
		<br>
		userSendOneUser
		<el-input v-model="username"></el-input>
		<el-input v-model="oneMsg"></el-input>
		<el-button type="primary" size="medium" @click="userSendOneUser">userSendOneUser</el-button>
	</div>
</template>

<script>
	export default {
		name: "Test",
		data() {
			return {
				serverMsg: '',
				allMsg: '',
				username: '',
				oneMsg: '',
				stompClient: null,
			}
		},
		mounted() {
			// this.connect()
		},
		methods: {
			connect() {
				let socket = new SockJS('http://localhost:8050/ws?token=' + window.sessionStorage.getItem('token'))
				this.stompClient = Stomp.over(socket)

				this.stompClient.connect({}, (frame) => {
					console.log(frame)
					this.stompClient.subscribe('/topic/sendAllUser', (response) => {
						console.log('sendAllUser')
						console.log(response)
					})
					this.stompClient.subscribe('/user/topic/sendOneUser', (response) => {
						console.log('sendOneUser')
						console.log(response)
					})
					this.stompClient.subscribe('/topic/userSendAllUser', (response) => {
						console.log('userSendAllUser')
						console.log(response)
					})
					this.stompClient.subscribe('/user/topic/userSendOneUser', (response) => {
						console.log('userSendOneUser')
						console.log(response)
					})
				})
			},
			userSendServer() {
				this.stompClient.send("/send/userSendServer", {}, this.serverMsg)
			},
			userSendAllUser() {
				this.stompClient.send("/send/userSendAllUser", {}, this.allMsg)
			},
			userSendOneUser() {
				this.stompClient.send("/send/userSendOneUser", {}, JSON.stringify({username: this.username, msg: this.oneMsg}))
			}
		}
	}
</script>

<style scoped>

</style>