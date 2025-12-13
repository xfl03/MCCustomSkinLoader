package customskinloader.fake.itf;

import customskinloader.fake.FakeMinecraftProfileTexture;

public interface IFakeResourceLocation {
    FakeMinecraftProfileTexture getTexture();

    void setTexture(FakeMinecraftProfileTexture texture);
}
