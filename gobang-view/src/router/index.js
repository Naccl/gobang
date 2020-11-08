import Vue from 'vue'
import VueRouter from 'vue-router'
import Index from "@/views/Index";

Vue.use(VueRouter)

const routes = [
	{
		path: '/',
		component: Index,
		meta: {
			title: '首页'
		}
	}
]

const router = new VueRouter({
	mode: 'history',
	base: process.env.BASE_URL,
	routes
})

export default router
