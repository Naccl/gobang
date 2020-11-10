<template>
	<div class="container">
		<h2>五子棋</h2>
		<div class="main">
			<div class="board">
				<canvas ref="canvas" :width="canvasWidth" :height="canvasHeight">Your browser does not support the HTML5 canvas tag.</canvas>
			</div>
			<div class="row">
				<el-button size="mini" type="primary" @click="restart">重新开始</el-button>
				<el-button size="mini" type="primary" @click="">悔棋</el-button>
				<el-button size="mini" type="primary" @click="">和棋</el-button>
			</div>
		</div>
	</div>
</template>

<script>
	export default {
		name: "Game",
		data() {
			return {
				margin: 30,//边距
				gridSpacing: 36,//网格间距
				rows: 15,//行数
				cols: 15,//列数
				canvas: null,
				context: null,
				lastStep: null,
				lastStatus: null,
				matrixChessBoard: [],//记录棋盘落子情况
				pieceColor: ["black", "white"], //棋子颜色
				step: 0, //记录当前步数
			}
		},
		computed: {
			canvasWidth() {
				return this.margin * 2 + this.gridSpacing * (this.cols - 1)
			},
			canvasHeight() {
				return this.margin * 2 + this.gridSpacing * (this.rows - 1)
			}
		},
		mounted() {
			this.init()
		},
		methods: {
			init() {
				this.canvas = this.$refs.canvas
				this.context = this.canvas.getContext("2d")
				this.drawBoard()
				this.initMatrixChessBoard()
				this.lastStep = this.context.getImageData(0, 0, this.canvasWidth, this.canvasHeight)
				this.lastStatus = this.context.getImageData(0, 0, this.canvasWidth, this.canvasHeight)
				this.canvas.addEventListener('click', e => this.clickEvent(e))
				this.canvas.addEventListener('mousemove', e => this.moveEvent(e))
				this.canvas.addEventListener('mouseleave', e => this.leaveEvent(e))
			},
			clickEvent(e) {
				let x = this.getX(e)
				let y = this.getY(e)
				if (this.canClick(x, y)) {
					//恢复棋盘状态（清空预落子红框）
					this.context.putImageData(this.lastStep, 0, 0)
					//落子
					this.drawChess(x, y, this.gridSpacing / 2.1, this.pieceColor[this.step % 2])
					//保存棋盘状态
					this.lastStep = this.context.getImageData(0, 0, this.canvasWidth, this.canvasHeight)
					//箭头标记最后一个棋子
					this.drawLastChess(x, y)
					this.lastStatus = this.context.getImageData(0, 0, this.canvasWidth, this.canvasHeight)
					this.matrixChessBoard[y][x] = this.pieceColor[this.step % 2]
					this.step++
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
			//判断点击位置是否存在棋子
			isExistChess(x, y) {
				return this.matrixChessBoard[y][x] ? true : false
			},
			//判断点击位置是否可以落子
			canClick(x, y) {
				const {rows, cols} = this
				return x >= 0 && y >= 0 && x < cols && y < rows && !this.isExistChess(x, y)
			},
			//绘制棋盘
			drawBoard() {
				const {margin, gridSpacing, rows, cols} = this
				let boardLineColor = '#555'
				for (let i = 0; i < rows; i++) {
					this.drawOnePixelLine(margin, margin + i * gridSpacing, margin + (cols - 1) * gridSpacing, margin + i * gridSpacing, boardLineColor)
				}
				for (let i = 0; i < cols; i++) {
					this.drawOnePixelLine(margin + i * gridSpacing, margin, margin + i * gridSpacing, margin + (rows - 1) * gridSpacing, boardLineColor)
				}
				//绘制棋盘上的五个点
				let radius = gridSpacing / 9
				let fiveDotColor = '#333'
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
			//绘制棋子
			drawChess(x, y, radius, color) {
				const {context, gridSpacing} = this
				let pixelX = this.getPixel(x)
				let pixelY = this.getPixel(y)
				let rad = gridSpacing / 4
				let r0 = color === 'black' ? 20 : 70
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
				this.lastStatus = this.context.getImageData(0, 0, this.canvasWidth, this.canvasHeight)
				this.step = 0
			},
		}
	}
</script>

<style scoped>
	.container {
		text-align: center;
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