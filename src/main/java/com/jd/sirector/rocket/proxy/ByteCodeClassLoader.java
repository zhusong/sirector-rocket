package com.jd.sirector.rocket.proxy;

import com.jd.sirector.rocket.utils.ClassUtils;
import javassist.ClassPool;

public class ByteCodeClassLoader extends ClassLoader {

    public ByteCodeClassLoader() {
        super(ByteCodeClassLoader.class.getClassLoader());
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        ClassPool cp = ClassUtils.getClassPool();

        byte[] bytes;
        try {
            bytes = cp.get(name).toBytecode();
        } catch (Exception var5) {
            var5.printStackTrace();
            return null;
        }


        return this.defineClass(name, bytes, 0, bytes.length);
    }

}