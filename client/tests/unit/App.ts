const { describe, it } = intern.getInterface('bdd');
import harness from '@dojo/framework/testing/harness';
import { w } from '@dojo/framework/widget-core/d';
//import Outlet from '@dojo/framework/routing/Outlet';

//import Menu from '../../src/widgets/Menu';
import HomeContainer from '../../src/containers/HomeContainer';
import About from '../../src/widgets/About';
import Profile from '../../src/pages/user/Profile';

import App from '../../src/App';
//import * as css from '../../src/App.m.css';

describe('App', () => {
	it('default renders correctly', () => {
		// const h = harness(() => w(App, {}));
		// h.expect(() =>
		// 	v('div', { classes: [css.root] }, [
		// 		w(Menu, {}),
		// 		v('div', [
		// 			w(Outlet, { key: 'home', id: 'home', renderer: () => w(Home, {}) }),
		// 			w(Outlet, { key: 'about', id: 'about', renderer: () => w(About, {}) }),
		// 			w(Outlet, { key: 'profile', id: 'profile', renderer: () => w(Profile, { username: 'Dojo User' }) })
		// 		])
		// 	])
		// );
	});

	it('home outlet renderer', () => {
		const h = harness(() => w(App, {}));
		const renderer = h.trigger('@home', 'renderer');
		h.expect(() => w(HomeContainer, {}), () => renderer);
	});

	it('about outlet renderer', () => {
		const h = harness(() => w(App, {}));
		const renderer = h.trigger('@about', 'renderer');
		h.expect(() => w(About, {}), () => renderer);
	});

	it('profile outlet renderer', () => {
		const h = harness(() => w(App, {}));
		const renderer = h.trigger('@profile', 'renderer');
		h.expect(() => w(Profile, { username: 'Dojo User' }), () => renderer);
	});
});
