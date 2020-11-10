import Vue from 'vue'
import VueRouter from 'vue-router'
import Index from "@/views/Index";
import Login from "@/views/login/Login";
import Home from "@/views/home/Home";
import Game from "@/views/game/Game";
import Test from "@/views/Test";

Vue.use(VueRouter)

const routes = [
	{
		path: '/',
		component: Index,
		meta: {
			title: '路由'
		}
	},
	{
		path: '/login',
		component: Login,
		meta: {
			title: '登录'
		}
	},
	{
		path: '/home',
		component: Home,
		meta: {
			title: '首页'
		}
	},
	{
		path: '/test',
		component: Test,
		meta: {
			title: '测试'
		}
	},
	{
		path: '/game',
		component: Game,
		meta: {
			title: '五子棋'
		}
	},
]

const router = new VueRouter({
	mode: 'history',
	base: process.env.BASE_URL,
	routes
})

export default router
