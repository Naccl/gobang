<template>
	<div class="site">
		<h2 class="title">五子棋</h2>
		<div class="container">
			<div class="left-column">
				<div class="owner">
					<h2>{{ owner }}</h2>
					<p>{{ ownerStatus }}</p>
				</div>

				<div class="player">
					<h2>{{ player }}</h2>
					<p>{{ playerStatus }}</p>
				</div>
			</div>
			<div class="main">
				<div class="board">
					<canvas ref="canvas" :width="canvasWidth" :height="canvasHeight">Your browser does not support the HTML5 canvas tag.</canvas>
				</div>
				<div class="row">
					<el-button size="mini" type="primary" @click="ready" :disabled="isReady">准备</el-button>
					<el-button size="mini" type="primary" @click="undo" :disabled="!isPlaying">悔棋</el-button>
					<el-button size="mini" type="primary" @click="heqi" :disabled="!isPlaying">和棋</el-button>
					<el-button size="mini" type="primary" @click="capitulate" :disabled="!isPlaying">认输</el-button>
					<el-button size="mini" type="primary" @click="exit">退出</el-button>
				</div>
			</div>
			<div class="right-column">
				聊天框
			</div>
		</div>
	</div>
</template>

<script>
	import {MessageBox} from 'element-ui'
	import {mapState} from 'vuex'

	export default {
		name: "Game",
		data() {
			return {
				subscribeList: [],
				me: '',//我的用户名
				owner: '',//房主的用户名
				player: '',//第二个玩家的用户名
				ownerStatus: '',//房主的状态
				playerStatus: '',//第二个玩家的状态
				isReady: false,//我是否已经准备
				isMe: false,//当前轮到我落子
				isPlaying: false,//游戏是否开始
				isHeqiWaiting: false,//申请和棋等待中
				isUndoWaiting: false,//申请悔棋等待中

				margin: 30,//边距
				gridSpacing: 36,//网格间距
				rows: 15,//行数
				cols: 15,//列数
				canvas: null,
				context: null,//canvas.getContext("2d")
				lastStep: null,//最后一个棋子落下时的棋盘状态
				lastStatus: null,//最后一个棋子被标记后的棋盘状态
				matrixChessBoard: [],//记录棋盘落子情况
			}
		},
		computed: {
			...mapState(['stompClient']),
			canvasWidth() {
				return this.margin * 2 + this.gridSpacing * (this.cols - 1)
			},
			canvasHeight() {
				return this.margin * 2 + this.gridSpacing * (this.rows - 1)
			}
		},
		beforeRouteLeave(to, from, next) {
			if (this.isPlaying) {
				this.$confirm('游戏已经开始，确认要退出吗？', '退出', {
					confirmButtonText: '退出',
					cancelButtonText: '取消'
				}).then(() => {
					this.stompClient.send('/send/exitRoom')
					this.unsubscribe()
					this.canvas.removeEventListener('click', e => this.clickEvent(e))
					this.canvas.removeEventListener('mousemove', e => this.moveEvent(e))
					this.canvas.removeEventListener('mouseleave', e => this.leaveEvent(e))
					next()
				}).catch(() => {
				})
			} else {
				this.stompClient.send('/send/exitRoom')
				this.unsubscribe()
				this.canvas.removeEventListener('click', e => this.clickEvent(e))
				this.canvas.removeEventListener('mousemove', e => this.moveEvent(e))
				this.canvas.removeEventListener('mouseleave', e => this.leaveEvent(e))
				next()
			}
		},
		created() {
			this.initSubscribe()
		},
		mounted() {
			this.initCanvas()
		},
		methods: {
			//准备
			ready() {
				if (this.me === this.owner) {
					this.ownerStatus = '准备'
				} else {
					this.playerStatus = '准备'
				}
				this.isReady = true
				this.stompClient.send("/send/ready", {}, this.owner)
			},
			//悔棋
			undo() {
				this.stompClient.send("/send/undo", {}, this.owner)
				this.isUndoWaiting = true
				this.$alert('等待对方同意悔棋......', '悔棋', {
					showClose: false,
					showConfirmButton: false
				})
			},
			//和棋
			heqi() {
				this.stompClient.send("/send/heqi", {}, this.owner)
				this.isHeqiWaiting = true
				this.$alert('等待对方同意和棋......', '和棋', {
					showClose: false,
					showConfirmButton: false
				})
			},
			//认输
			capitulate() {
				this.$confirm('确认要认输吗？', '认输', {
					confirmButtonText: '认输',
					cancelButtonText: '取消'
				}).then(() => {
					this.stompClient.send("/send/capitulate", {}, this.owner)
				}).catch(() => {
					this.msgInfo('取消认输')
				})
			},
			//退出游戏房间
			exit() {
				this.$router.push('/home')
			},
			//初始化订阅消息
			initSubscribe() {
				//获取当前游戏对局信息
				this.subscribeList.push(this.stompClient.subscribe('/send/game', response => {
					const resp = JSON.parse(response.body)
					this.me = resp.data.me
					this.owner = resp.data.owner
					this.player = resp.data.player
					this.ownerStatus = resp.data.ownerReady ? '准备' : ''
					this.playerStatus = resp.data.playerReady ? '准备' : ''
				}))
				//房主订阅玩家进入房间的消息
				this.subscribeList.push(this.stompClient.subscribe('/user/topic/enterRoom', response => {
					const resp = JSON.parse(response.body)
					this.player = resp.data
				}))
				//订阅对手准备消息
				this.subscribeList.push(this.stompClient.subscribe('/user/topic/ready', response => {
					const resp = JSON.parse(response.body)
					if (resp.data === this.owner) {
						this.ownerStatus = '准备'
					} else {
						this.playerStatus = '准备'
					}
				}))
				//订阅游戏开始消息
				this.subscribeList.push(this.stompClient.subscribe('/user/topic/start', response => {
					const resp = JSON.parse(response.body)
					if (resp.data === this.owner) {
						this.ownerStatus = '黑棋'
						this.playerStatus = '白棋'
					} else {
						this.ownerStatus = '白棋'
						this.playerStatus = '黑棋'
					}
					if (resp.data === this.me) {
						this.isMe = true
					} else {
						this.isMe = false
					}
					this.isPlaying = true
					this.restart()
				}))
				//订阅落子消息
				this.subscribeList.push(this.stompClient.subscribe('/user/topic/setChess', response => {
					const resp = JSON.parse(response.body)
					this.setChessHandler(resp.data.x, resp.data.y, resp.data.isBlack)
				}))
				//订阅胜负或平局消息
				this.subscribeList.push(this.stompClient.subscribe('/user/topic/win', response => {
					const resp = JSON.parse(response.body)
					this.$message({
						showClose: true,
						message: resp.data,
						type: 'success',
						duration: 5000
					})
					this.isPlaying = false
					this.isMe = false
					this.isReady = false
					this.ownerStatus = ''
					this.playerStatus = ''
					if (this.isHeqiWaiting) {
						MessageBox.close()
						this.isHeqiWaiting = false
					}
				}))
				//订阅对方请求和棋消息
				this.subscribeList.push(this.stompClient.subscribe('/user/topic/heqi', response => {
					this.$confirm('对方请求和棋，是否同意？', '和棋', {
						showClose: false,
						closeOnClickModal: false,
						closeOnPressEscape: false,
						confirmButtonText: '同意',
						cancelButtonText: '不同意'
					}).then(() => {
						this.stompClient.send('/send/agreeHeqi', {}, this.owner)
					}).catch(() => {
						this.stompClient.send('/send/refuseHeqi', {}, this.owner)
						this.msgInfo('不同意和棋')
					})
				}))
				//订阅对方拒绝和棋消息
				this.subscribeList.push(this.stompClient.subscribe('/user/topic/refuseHeqi', response => {
					const resp = JSON.parse(response.body)
					this.msgInfo(resp.data)
					MessageBox.close()
					this.isHeqiWaiting = false
				}))
				//订阅对方请求悔棋消息
				this.subscribeList.push(this.stompClient.subscribe('/user/topic/undo', response => {
					this.$confirm('对方请求悔棋，是否同意？', '悔棋', {
						showClose: false,
						closeOnClickModal: false,
						closeOnPressEscape: false,
						confirmButtonText: '同意',
						cancelButtonText: '不同意'
					}).then(() => {
						this.stompClient.send('/send/agreeUndo', {}, this.owner)
					}).catch(() => {
						this.stompClient.send('/send/refuseUndo', {}, this.owner)
						this.msgInfo('不同意悔棋')
					})
				}))
				//订阅对方拒绝悔棋消息
				this.subscribeList.push(this.stompClient.subscribe('/user/topic/refuseUndo', response => {
					const resp = JSON.parse(response.body)
					this.msgInfo(resp.data)
					MessageBox.close()
					this.isUndoWaiting = false
				}))
				//双方都要订阅同意悔棋消息
				this.subscribeList.push(this.stompClient.subscribe('/user/topic/agreeUndo', response => {
					const resp = JSON.parse(response.body)
					if (this.isUndoWaiting) {
						MessageBox.close()
						this.isUndoWaiting = false
					}
					if (resp.data.now === this.me) {
						this.isMe = true
					} else {
						this.isMe = false
					}
					this.matrixChessBoard = resp.data.matrix
					this.drawUndoMatrix()
					this.lastStep = this.context.getImageData(0, 0, this.canvasWidth, this.canvasHeight)
					if (resp.data.lastX) {
						this.drawLastChess(resp.data.lastY, resp.data.lastX)
					}
					this.lastStatus = this.context.getImageData(0, 0, this.canvasWidth, this.canvasHeight)
				}))
			},
			//取消所有订阅
			unsubscribe() {
				this.subscribeList.forEach(sub => {
					sub.unsubscribe()
				})
				this.subscribeList = []
			},
			//初始化棋盘
			initCanvas() {
				this.canvas = this.$refs.canvas
				this.context = this.canvas.getContext("2d")
				this.drawBoard()
				this.initMatrixChessBoard()
				this.lastStep = this.context.getImageData(0, 0, this.canvasWidth, this.canvasHeight)
				this.lastStatus = this.lastStep
				this.canvas.addEventListener('click', e => this.clickEvent(e))
				this.canvas.addEventListener('mousemove', e => this.moveEvent(e))
				this.canvas.addEventListener('mouseleave', e => this.leaveEvent(e))
			},
			clickEvent(e) {
				let x = this.getX(e)
				let y = this.getY(e)
				if (this.canClick(x, y)) {
					//发送落子消息
					const data = {
						owner: this.owner,
						x,
						y
					}
					this.stompClient.send('/send/setChess', {}, JSON.stringify(data))
				}
			},
			moveEvent(e) {
				let x = this.getX(e)
				let y = this.getY(e)
				this.drawPreloadingRedFrame(x, y)
			},
			leaveEvent(e) {
				this.context.putImageData(this.lastStatus, 0, 0)
			},
			//绘制最后一个棋子的标记
			drawLastChess(x, y) {
				const {gridSpacing} = this
				let length = gridSpacing / 6
				let interspace = 2
				let color = 'red'
				let centerX = this.getPixel(x), centerY = this.getPixel(y)
				let topX = centerX, topY = centerY - length
				let bottomX = centerX, bottomY = centerY + length
				let leftX = centerX - length, leftY = centerY
				let rightX = centerX + length, rightY = centerY
				this.drawLine(centerX, centerY - interspace, topX, topY, color)
				this.drawLine(centerX, centerY + interspace, bottomX, bottomY, color)
				this.drawLine(centerX - interspace, centerY, leftX, leftY, color)
				this.drawLine(centerX + interspace, centerY, rightX, rightY, color)
			},
			//绘制预落子红框
			drawPreloadingRedFrame(x, y) {
				const {gridSpacing, context} = this
				if (this.canClick(x, y)) {
					//设置鼠标样式
					this.canvas.style.cursor = 'pointer'
					//绘制预落子框前，先恢复棋盘状态（清空其它预落子红框）
					context.putImageData(this.lastStatus, 0, 0)
					let length = gridSpacing / 4
					let radius = gridSpacing / 2
					let color = 'red'
					let absX = this.getPixel(x), absY = this.getPixel(y)
					let topLeftX = absX - radius, topLeftY = absY - radius
					let topRightX = absX + radius, topRightY = absY - radius
					let bottomLeftX = absX - radius, bottomLeftY = absY + radius
					let bottomRightX = absX + radius, bottomRightY = absY + radius
					//左上角
					this.drawLine(topLeftX, topLeftY, topLeftX, topLeftY + length, color)
					this.drawLine(topLeftX, topLeftY, topLeftX + length, topLeftY, color)
					//右上角
					this.drawLine(topRightX, topRightY, topRightX, topRightY + length, color)
					this.drawLine(topRightX, topRightY, topRightX - length, topRightY, color)
					//左下角
					this.drawLine(bottomLeftX, bottomLeftY, bottomLeftX, bottomLeftY - length)
					this.drawLine(bottomLeftX, bottomLeftY, bottomLeftX + length, bottomLeftY)
					//右下角
					this.drawLine(bottomRightX, bottomRightY, bottomRightX, bottomRightY - length)
					this.drawLine(bottomRightX, bottomRightY, bottomRightX - length, bottomRightY)
				} else {
					//不能落子，恢复棋盘状态（清空预落子红框）
					context.putImageData(this.lastStatus, 0, 0)
					//设置鼠标样式
					this.canvas.style.cursor = 'default'
				}
			},
			//画线条
			drawLine(fromX, fromY, toX, toY, color) {
				const {context} = this
				context.strokeStyle = color
				context.beginPath()
				context.moveTo(fromX, fromY)
				context.lineTo(toX, toY)
				context.closePath()
				context.stroke()
			},
			//画单像素的线条
			drawOnePixelLine(fromX, fromY, toX, toY, color) {
				const {context} = this
				context.strokeStyle = color
				context.save()
				context.translate(0.5, 0.5)
				context.lineWidth = 1.00
				context.strokeStyle = color
				context.beginPath()
				context.moveTo(fromX, fromY)
				context.lineTo(toX, toY)
				context.closePath()
				context.stroke()
				context.restore()
			},
			//获取点击事件在棋盘上对应的坐标
			getX(e) {
				const {margin, gridSpacing} = this
				return Math.floor((e.offsetX - margin + gridSpacing / 2) / gridSpacing)
			},
			getY(e) {
				const {margin, gridSpacing} = this
				return Math.floor((e.offsetY - margin + gridSpacing / 2) / gridSpacing)
			},
			//获取落点的实际像素坐标
			getPixel(i) {
				const {margin, gridSpacing} = this
				return i * gridSpacing + margin
			},
			//判断点击位置是否可以落子
			canClick(x, y) {
				return this.isMe && x >= 0 && y >= 0 && x < this.cols && y < this.rows && !this.matrixChessBoard[y][x]
			},
			//绘制棋盘
			drawBoard() {
				const {margin, gridSpacing, rows, cols} = this
				let boardLineColor = '#000'
				for (let i = 0; i < rows; i++) {
					this.drawOnePixelLine(margin, margin + i * gridSpacing, margin + (cols - 1) * gridSpacing, margin + i * gridSpacing, boardLineColor)
				}
				for (let i = 0; i < cols; i++) {
					this.drawOnePixelLine(margin + i * gridSpacing, margin, margin + i * gridSpacing, margin + (rows - 1) * gridSpacing, boardLineColor)
				}
				//绘制棋盘上的五个点
				let radius = gridSpacing / 9
				let fiveDotColor = '#000'
				this.drawDot(Math.floor(cols / 2), Math.floor(rows / 2), radius, fiveDotColor)
				this.drawDot(3, 3, radius, fiveDotColor)
				this.drawDot(cols - 4, 3, radius, fiveDotColor)
				this.drawDot(3, rows - 4, radius, fiveDotColor)
				this.drawDot(cols - 4, rows - 4, radius, fiveDotColor)
			},
			//初始化棋盘数组
			initMatrixChessBoard() {
				const {rows, cols} = this
				for (let i = 0; i < rows; i++) {
					this.matrixChessBoard[i] = []
					for (let j = 0; j < cols; j++) {
						this.matrixChessBoard[i][j] = 0
					}
				}
			},
			//绘制点
			drawDot(x, y, radius, color) {
				const {context} = this
				context.beginPath()
				context.arc(this.getPixel(x), this.getPixel(y), radius, 0, 2 * Math.PI)
				context.closePath()
				context.fillStyle = color
				context.fill()
			},
			//棋盘落子处理
			setChessHandler(x, y, isBlack) {
				//恢复棋盘状态（清空预落子红框）
				this.context.putImageData(this.lastStep, 0, 0)
				//绘制棋子
				this.drawChess(x, y, isBlack)
				//保存棋盘状态
				this.lastStep = this.context.getImageData(0, 0, this.canvasWidth, this.canvasHeight)
				//箭头标记最后一个棋子
				this.drawLastChess(x, y)
				this.lastStatus = this.context.getImageData(0, 0, this.canvasWidth, this.canvasHeight)
				this.matrixChessBoard[y][x] = isBlack ? 1 : 2
				this.isMe = !this.isMe
			},
			//绘制棋子
			drawChess(x, y, isBlack) {
				const {context, gridSpacing} = this
				let pixelX = this.getPixel(x)
				let pixelY = this.getPixel(y)
				let radius = this.gridSpacing / 2.1
				let rad = gridSpacing / 4
				let r0 = isBlack ? 20 : 100
				const gradient = context.createRadialGradient(pixelX + rad, pixelY - rad, r0, pixelX + rad, pixelY - rad, 0)
				gradient.addColorStop(0, '#000')
				gradient.addColorStop(1, '#fff')
				context.beginPath()
				context.arc(pixelX, pixelY, radius, 0, 2 * Math.PI)
				context.closePath()
				context.fillStyle = gradient
				context.fill()
			},
			//重新开始
			restart() {
				const {context} = this
				context.fillStyle = "#FFD577"
				context.fillRect(0, 0, this.canvasWidth, this.canvasHeight)
				this.drawBoard()
				this.initMatrixChessBoard()
				this.lastStep = this.context.getImageData(0, 0, this.canvasWidth, this.canvasHeight)
				this.lastStatus = this.lastStep
			},
			//根据棋盘状态重新绘制（回到悔棋后的状态）
			drawUndoMatrix() {
				const {context, matrixChessBoard} = this
				context.fillStyle = "#FFD577"
				context.fillRect(0, 0, this.canvasWidth, this.canvasHeight)
				this.drawBoard()
				for (let i in matrixChessBoard) {
					for (let j in matrixChessBoard[i]) {
						if (matrixChessBoard[i][j] === 1) {
							this.drawChess(j, i, true)
						} else if (matrixChessBoard[i][j] === 2) {
							this.drawChess(j, i, false)
						}
					}
				}
			},
		}
	}
</script>

<style scoped>
	.site {
		width: 1300px;
		margin-left: auto;
		margin-right: auto;
	}

	.title {
		text-align: center;
	}

	.container {
		width: 1300px;
		text-align: center;
		margin-left: auto;
		margin-right: auto;
		display: -webkit-box;
		display: -ms-flexbox;
		display: flex;
		-webkit-box-orient: horizontal;
		-webkit-box-direction: normal;
		-ms-flex-direction: row;
		flex-direction: row;
		-ms-flex-wrap: wrap;
		flex-wrap: wrap;
		-webkit-box-align: stretch;
		-ms-flex-align: stretch;
		align-items: stretch;
		padding: 0;
	}

	.left-column, .right-column {
		width: 27.5%;
		position: relative;
		display: inline-block;
		vertical-align: top;
	}

	.left-column .player {
		margin-top: 200px;
	}

	.main {
		width: 45%;
	}

	.main canvas {
		background-color: #FFD577;
	}

	.main .row {
		margin-top: 15px;
	}

	.main .row button + button {
		margin-left: 50px;
	}
</style>