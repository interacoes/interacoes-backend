
UPDATE eperson
SET
    password = '5d5f7b8fbd2a5752398107bae459177e31a06a0b804bfe42e00e9deba780f323f53b2c77cf740b95bb8129f4c7325f2d86dd06c16abd59a7999876c3b34fbc48',
    salt = 'bd31e5bc40771bf9ee115047746b2977',
    digest_algorithm = 'SHA-512'
WHERE email = 'catalogador@ltap.ifce.edu.br';


UPDATE eperson
SET
    password = '11a8c9399e41d1dd4165b7463347fd8cb58e058ef7cb8ce5ae36505903f8bfc7c5143ae03b2cce910fb1c3d1ca426d119633c726ed166e56b5954d40424ec503',
    salt = '819cacb57085485fea1f82256e62fb04',
    digest_algorithm = 'SHA-512'
WHERE email = 'curador@ltap.ifce.edu.br';
