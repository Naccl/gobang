<template>
	<div class="container">
		<h1>游戏大厅</h1>
		<p>当前在线玩家数: {{ playerCount }}</p>
		<p>当前房间个数: {{ roomCount }}</p>
		<el-button type="primary" size="mini" @click="connect">重新连接</el-button>
		<el-button type="primary" size="mini" @click="createRoom">创建房间</el-button>
		<ul class="room-list">
			<li class="room" v-for="(room,index) in roomList" :key="index">
				<span class="user">{{ room.owner }}</span>
				<span class="table">VS</span>
				<span class="user">{{ room.player }}</span>
				<el-button type="primary" size="mini" v-if="!room.playing" @click="enterRoom(room.owner)">加入房间</el-button>
				<el-tag effect="dark" size="medium" v-else>游戏中</el-tag>
			</li>
		</ul>
	</div>
</template>

<script>
	import {mapState} from 'vuex'
	import {SAVE_STOMP_CLIENT} from "@/store/mutations-types";

	export default {
		name: "Home",
		data() {
			return {
				subscribeList: [],
				playerCount: 0,
				roomCount: 0,
				roomList: []
			}
		},
		computed: {
			...mapState(['stompClient'])
		},
		created() {
			this.connect()
		},
		beforeRouteLeave(to, from, next) {
			this.unsubscribe()
			next()
		},
		methods: {
			connect() {
				if (this.stompClient && this.stompClient.connected) {
					this.unsubscribe()
					this.subscribe()
				} else {
					this.newConnect()
				}
			},
			newConnect() {
				let socket = new SockJS('http://localhost:8050/ws?token=' + window.sessionStorage.getItem('token'))
				let stompClient = Stomp.over(socket)
				this.$store.commit(SAVE_STOMP_CLIENT, stompClient)
				this.stompClient.debug = null
				this.stompClient.connect({}, this.subscribe, () => {
					this.msgError('连接失败，请重试')
				})
			},
			subscribe() {
				//获取当前游戏大厅状态
				this.subscribeList.push(this.stompClient.subscribe('/send/home', response => {
					const resp = JSON.parse(response.body)
					this.playerCount = resp.data.playerCount
					this.roomCount = resp.data.roomCount
					this.roomList = resp.data.roomList
				}))
				//订阅当前在线玩家数
				this.subscribeList.push(this.stompClient.subscribe('/topic/playerCount', response => {
					const resp = JSON.parse(response.body)
					this.playerCount = resp.data
				}))
				//订阅大厅房间创建消息
				this.subscribeList.push(this.stompClient.subscribe('/topic/createRoom', response => {
					const resp = JSON.parse(response.body)
					this.roomList.push(resp.data)
					this.roomCount++
				}))
				//订阅大厅房间进入消息
				this.subscribeList.push(this.stompClient.subscribe('/topic/enterRoom', response => {
					const resp = JSON.parse(response.body)
					this.roomList.some(room => {
						if (room.owner === resp.data.owner) {
							return room.player = resp.data.player
						}
					})
				}))
				//订阅大厅房间移除消息
				this.subscribeList.push(this.stompClient.subscribe('/topic/removeRoom', response => {
					const resp = JSON.parse(response.body)
					this.roomCount = resp.data.roomCount
					for (let i in this.roomList) {
						if (this.roomList[i].owner === resp.data.owner) {
							this.roomList.splice(i, 1)
							return
						}
					}
				}))
				//订阅大厅房间更新消息
				this.subscribeList.push(this.stompClient.subscribe('/topic/updateRoom', response => {
					const resp = JSON.parse(response.body)
					this.roomList.some(room => {
						if (room.owner === resp.data.owner) {
							room.owner = resp.data.room.owner
							room.player = resp.data.room.player
							room.playing = resp.data.room.playing
							return true
						}
					})
				}))
				//订阅自己创建房间成功或失败消息
				this.subscribeList.push(this.stompClient.subscribe('/user/topic/createRoom', response => {
					const resp = JSON.parse(response.body)
					if (resp.code === 200) {
						this.$router.push('/game')
					} else {
						this.msgError(resp.msg)
					}
				}))
				//订阅自己进入房间成功或失败消息
				this.subscribeList.push(this.stompClient.subscribe('/user/topic/enterRoom', response => {
					const resp = JSON.parse(response.body)
					if (resp.code === 200) {
						this.$router.push('/game')
					} else {
						this.msgError(resp.msg)
					}
				}))
			},
			//取消所有订阅
			unsubscribe() {
				this.subscribeList.forEach(sub => {
					sub.unsubscribe()
				})
				this.subscribeList = []
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