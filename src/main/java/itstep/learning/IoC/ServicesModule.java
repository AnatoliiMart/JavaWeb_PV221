package itstep.learning.IoC;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import itstep.learning.services.formParse.FormParseService;
import itstep.learning.services.formParse.MixedFormParseService;
import itstep.learning.services.hash.HashService;
import itstep.learning.services.hash.Md5HashService;
import itstep.learning.services.hash.ShaHashService;
import itstep.learning.services.stream.BaosStringReader;
import itstep.learning.services.stream.StringReader;

public class ServicesModule extends AbstractModule {
    private final StringReader reader;

    public ServicesModule(StringReader reader) {
        this.reader = reader;
    }

    @Override
    protected void configure() {
        bind( HashService.class )
                .annotatedWith( Names.named("digest") )
                .to( Md5HashService.class ) ;

        bind( HashService.class )
                .annotatedWith( Names.named("signature") )
                .to( ShaHashService.class ) ;
        bind(FormParseService.class)
                .to(MixedFormParseService.class);
        bind(StringReader.class).toInstance(reader);
    }
}
