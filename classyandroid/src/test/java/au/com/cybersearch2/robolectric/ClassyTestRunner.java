package au.com.cybersearch2.robolectric;

import java.io.File;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;

public class ClassyTestRunner extends RobolectricTestRunner 
{

    public ClassyTestRunner(Class<?> testClass) throws InitializationError 
    {
        super(testClass);
    }
 
    /**
     * Create Manifest which gets libraries from location "target/unpacked-libs"
     * @see org.robolectric.RobolectricTestRunner#createAppManifest(org.robolectric.res.FsFile, org.robolectric.res.FsFile, org.robolectric.res.FsFile)
     */
    @Override 
    protected AndroidManifest createAppManifest(FsFile manifestFile, FsFile resDir, FsFile assetsDir) 
    {
        return new MavenAndroidManifest(Fs.newFile(new File(".")));
    }
}