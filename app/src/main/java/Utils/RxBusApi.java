package Utils;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by xcaluser on 23/5/17.
 */

public class RxBusApi {
    private final Subject<Object, Object> _bus = new SerializedSubject<>(PublishSubject.create());

    public void send(Object o) {


        _bus.onNext(o);
    }
    public void complete() {


        _bus.onCompleted();
    }
    public Observable<Object> toObserverable() {

        return _bus;
    }
}
