(function framework7ComponentLoader(e,n){void 0===n&&(n=!0);document,window;var t=e.$,o=(e.Template7,e.utils),r=(e.device,e.support,e.Class,e.Modal),i=(e.ConstructorMethods,e.ModalMethods),s=function(e){function n(n,r){var i=o.extend({on:{}},r);e.call(this,n,i);var s;return this.params=i,(s=this.params.el?t(this.params.el).eq(0):t(this.params.content).filter((function(e,n){return 1===n.nodeType})).eq(0))&&s.length>0&&s[0].f7Modal?s[0].f7Modal:0===s.length?this.destroy():(o.extend(this,{app:n,$el:s,el:s[0],type:"loginScreen"}),s[0].f7Modal=this,this)}return e&&(n.__proto__=e),n.prototype=Object.create(e&&e.prototype),n.prototype.constructor=n,n}(r),a={name:"loginScreen",static:{LoginScreen:s},create:function(){this.loginScreen=i({app:this,constructor:s,defaultSelector:".login-screen.modal-in"})},clicks:{".login-screen-open":function(e,n){void 0===n&&(n={});this.loginScreen.open(n.loginScreen,n.animate,e)},".login-screen-close":function(e,n){void 0===n&&(n={});this.loginScreen.close(n.loginScreen,n.animate,e)}}};if(n){if(e.prototype.modules&&e.prototype.modules[a.name])return;e.use(a),e.instance&&(e.instance.useModuleParams(a,e.instance.params),e.instance.useModule(a))}return a}(Framework7, typeof Framework7AutoInstallComponent === 'undefined' ? undefined : Framework7AutoInstallComponent))
