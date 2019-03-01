var JSAPI = Java.type('customskinloader.forge.loader.ModLauncher');
function f(cn) {
    return JSAPI.instance().transform(cn);
}
function initializeCoreMod() {
    return {
        'SkinManagerTransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/resources/SkinManager'
            },
            'transformer': function(cn) {
                return f(cn);
            }
        },
        'PlayerTabTransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/gui/GuiPlayerTabOverlay'
            },
            'transformer': function(cn) {
                return f(cn);
            }
        },
        'FakeSkinManagerTransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'customskinloader/fake/FakeSkinManager'
            },
            'transformer': function(cn) {
                return f(cn);
            }
        }
    };
}