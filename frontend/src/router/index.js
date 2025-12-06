import { createRouter, createWebHistory } from 'vue-router'

import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import ProfileView from '../views/ProfileView.vue'
import PostsListView from '../views/PostsListView.vue'
import EventsView from '../views/EventsView.vue'
import FishingMapView from '../views/FishingMapView.vue'

const routes = [
    { path: '/', redirect: '/login' },
    { path: '/login', name: 'login', component: LoginView },
    { path: '/register', name: 'register', component: RegisterView },
    { path: '/profile', name: 'profile', component: ProfileView },
    { path: '/events', name: 'events', component: EventsView, meta: { requiresAuth: true } },
    { path: '/posts', name: 'posts', component: PostsListView, meta: { requiresAuth: true } },
    { path: '/map', name: 'map', component: FishingMapView },
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

export default router
