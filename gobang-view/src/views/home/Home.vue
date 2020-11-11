<template>
	<div class="container">
		<h1>游戏大厅</h1>
		<p>当前在线玩家数: {{ playerCount }}</p>
		<p>当前房间个数: {{ roomCount }}</p>
		<el-button type="primary" size="mini" @click="createRoom">创建房间</el-button>
		<ul class="room-list">
			<li class="room" v-for="(room,index) in roomList" :key="index">
				<span class="user">{{ room.owner }}</span>
				<span class="table">VS</span>
				<span class="user">{{ room.player }}</span>
				<el-button type="primary" size="mini" v-if="!room.isPlaying" @click="enterRoom(room.owner)">加入房间</el-button>
				<el-button type="primary" size="mini" v-else>观战</el-button>
			</li>
		</ul>
	</div>
</template>

<script>
	export default {
		name: "Home",
		data() {
			return {
				stompClient: null,
				playerCount: 0,
				roomCount: 0,
				roomList: []
			}
		},
		created() {
			this.connect()
		},
		methods: {
			connect() {
				let socket = new SockJS('http://localhost:8050/ws?token=' + window.sessionStorage.getItem('token'))
				this.stompClient = Stomp.over(socket)
				this.stompClient.debug = null
				this.stompClient.connect({}, this.subscribe)
			},
			subscribe() {
				//获取当前游戏大厅状态
				this.stompClient.subscribe('/send/home', response => {
					const resp = JSON.parse(response.body)
					this.playerCount = resp.data.playerCount
					this.roomCount = resp.data.roomCount
					this.roomList = resp.data.roomList
				})
				//订阅当前在线玩家数
				this.stompClient.subscribe('/topic/playerCount', response => {
					const resp = JSON.parse(response.body)
					this.playerCount = resp.data
				})
				//订阅大厅房间创建消息
				this.stompClient.subscribe('/topic/createRoom', response => {
					const resp = JSON.parse(response.body)
					this.roomList.push(resp.data)
					this.roomCount += 1
				})
				//订阅自己创建房间失败消息
				this.stompClient.subscribe('/user/topic/createRoom', response => {
					const resp = JSON.parse(response.body)
					this.msgError(resp.msg)
				})
				//订阅大厅房间进入消息
				this.stompClient.subscribe('/topic/enterRoom', response => {
					const resp = JSON.parse(response.body)
					this.roomList.some(room => {
						if (room.owner === resp.data.owner) {
							return room.player = resp.data.player
						}
					})
				})
				//订阅自己进入房间失败消息
				this.stompClient.subscribe('/user/topic/enterRoom', response => {
					const resp = JSON.parse(response.body)
					this.msgError(resp.msg)
				})
			},
			createRoom() {
				this.stompClient.send("/send/createRoom")
			},
			enterRoom(owner) {
				this.stompClient.send("/send/enterRoom", {}, owner)
			}
		}
	}
</script>

<style scoped>
	.container {
		text-align: center;
	}

	.room-list {
		list-style: none;
	}

	.room {
		margin-bottom: 20px;
	}

	.room span {
		margin-right: 10px;
	}

	.room .user {
		color: red;
	}

	.room .table {

	}
</style>