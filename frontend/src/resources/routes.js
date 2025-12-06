import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import ProfileView from '../views/ProfileView.vue'
import PostsListView from '../views/PostsListView.vue'
import FishingMapView from '../views/FishingMapView.vue'

export const routes = [
    { path: '/', redirect: '/login' },
    { path: '/login', name: 'login', component: LoginView },
    { path: '/register', name: 'register', component: RegisterView },
    { path: '/profile', name: 'profile', component: ProfileView },
    { path: '/posts', name: 'posts', component: PostsListView, meta: { requiresAuth: true } },
    { path: '/map', name: 'map', component: FishingMapView },
]
