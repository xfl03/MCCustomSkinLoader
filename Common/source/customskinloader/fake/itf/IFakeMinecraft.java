package customskinloader.fake.itf;

import java.util.concurrent.Executor;

import com.google.common.util.concurrent.ListenableFuture;

public interface IFakeMinecraft extends Executor {
    default ListenableFuture<Object> func_152344_a(Runnable runnable) {
        this.execute(runnable);
        return null;
    }

    @Override
    default void execute(Runnable runnable) {
        this.func_152344_a(runnable);
    }
}
