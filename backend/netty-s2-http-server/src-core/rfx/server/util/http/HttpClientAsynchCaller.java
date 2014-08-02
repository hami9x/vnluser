package rfx.server.util.http;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observable.OnSubscribe;
import rx.Observer;
import rx.Subscriber;

public class HttpClientAsynchCaller {
	ExecutorService executor;

	public HttpClientAsynchCaller() {
		super();
		this.executor = new ThreadPoolExecutor(4, 4, 1, TimeUnit.MINUTES,
				new LinkedBlockingQueue<Runnable>());
	}

	protected static class CallWebServiceTask implements Callable<Void> {
		Subscriber<? super String> subscriber;
		String url;

		public CallWebServiceTask(Subscriber<? super String> subscriber,
				String url) {
			super();
			this.subscriber = subscriber;
			this.url = url;
		}

		@Override
		public Void call() throws Exception {
			subscriber.onNext(synchronizedCall(url));
			return null;
		}
	};

	public static String synchronizedCall(String url) {
		return HttpClientUtil.executeGet(url);
	}

	public Observable<String> call(final String url) {
		return Observable.create(new OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> subscriber) {
				if (subscriber.isUnsubscribed()) {
					return;
				}
				try {
					executor.submit(new CallWebServiceTask(subscriber, url)).get();
				} catch (Exception e) {
					subscriber.onError(e);
				}
				subscriber.onCompleted();
				executor.shutdownNow();
				return;
			}
		});
	}

	public Observable<String> call(final String... urls) {
		return Observable.create(new OnSubscribe<String>() {
			@Override
			public void call(Subscriber<? super String> subscriber) {
				for (String url : urls) {
					if (subscriber.isUnsubscribed()) {
						return;
					}
					try {
						executor.submit(new CallWebServiceTask(subscriber, url))
								.get();
					} catch (Exception e) {
						subscriber.onError(e);
					}
				}
				subscriber.onCompleted();
				executor.shutdownNow();
				return;
			}
		});
	}

	public static void main(String[] args) {
		String url1 = "http://www.javacodegeeks.com/2013/07/java-futuretask-example-program.html";
		String url2 = "http://techblog.netflix.com/2013/02/rxjava-netflix-api.html";
		String url3 = "https://github.com/Netflix/RxJava/wiki/Creating-Observables";

		new HttpClientAsynchCaller().call(url1, url2, url3).subscribe(
				new Observer<String>() {
					@Override
					public void onCompleted() {
						System.out.println("Done!");
					}

					@Override
					public void onError(Throwable arg0) {
						// TODO Auto-generated method stub
					}

					@Override
					public void onNext(String html) {
						System.out.println(html);
					}
				});
	

	}
}
