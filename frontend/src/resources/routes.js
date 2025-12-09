import LoginView from '../views/LoginView.vue'
import RegisterView from '../views/RegisterView.vue'
import ProfileView from '../views/ProfileView.vue'
import PostsListView from '../views/PostsListView.vue'
import EventsView from '../views/EventsView.vue'
import FishingMapView from '../views/FishingMapView.vue'
import GuidesView from '../views/GuidesView.vue'
import GuideDetailsPanel from '../components/guides/GuideDetailsPanel.vue'
import GuideCreatePanel from '../components/guides/GuideCreatePanel.vue'
import PostDetailsView from "../views/PostDetailsView.vue";

export const routes = [
    { path: '/', redirect: '/login' },
    { path: '/login', name: 'login', component: LoginView },
    { path: '/register', name: 'register', component: RegisterView },
    { path: '/profile', name: 'profile', component: ProfileView },
    { path: '/events', name: 'events', component: EventsView, meta: { requiresAuth: true } },
    { path: '/posts', name: 'posts', component: PostsListView, meta: { requiresAuth: true } },
    { path: '/map', name: 'map', component: FishingMapView },
    { path: '/guides', name: 'guides', component: GuidesView },
    { path: '/guides/:id', name: 'guides.details', component: GuideDetailsPanel },
    { path: '/guides/create', name: 'guides.create', component: GuideCreatePanel },
    { path: '/posts/:id', name: 'postDetails', component: PostDetailsView, meta: { requiresAuth: true }, props: true,},
]
