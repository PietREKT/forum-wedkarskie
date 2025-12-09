import { createRouter, createWebHistory } from 'vue-router'

import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import ProfileView from '../views/ProfileView.vue'
import PostsListView from '../views/PostsListView.vue'
import PostDetailsView from '../views/PostDetailsView.vue'
import EventsView from '../views/EventsView.vue'
import FishingMapView from '../views/FishingMapView.vue'
import GuidesView from '../views/GuidesView.vue'

const routes = [
    { path: '/', redirect: '/login' },
    { path: '/login', name: 'login', component: LoginView },
    { path: '/register', name: 'register', component: RegisterView },
    { path: '/profile', name: 'profile', component: ProfileView },
    { path: '/events', name: 'events', component: EventsView, meta: { requiresAuth: true } },
    { path: '/posts', name: 'posts', component: PostsListView, meta: { requiresAuth: true } },
    { path: '/posts/:id', name: 'postDetails', component: PostDetailsView, meta: { requiresAuth: true }, props: true,},
    { path: '/map', name: 'map', component: FishingMapView },
    { path: '/guides', name: 'guides', component: GuidesView },
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

export default router
