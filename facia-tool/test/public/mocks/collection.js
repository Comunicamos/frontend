import mockjax from 'test/utils/mockjax';
import _ from 'underscore';
import mediator from 'utils/mediator';

var all = {};

mockjax({
    url: /collection\/(.+)/,
    urlParams: ['collection'],
    type: 'get',
    response: function (req) {
        this.responseText = all[req.urlParams.collection];
    },
    onAfterComplete: function () {
        mediator.emit('mock:collection');
    }
});

export function set (collections) {
    all = _.extend(all, collections);
}
