create table PRODUCTS
(
  id 	      BIGINT not null,
  name        VARCHAR(255) not null,
  price       INT not null
) ;

insert into products (id, name, price)
values (1, 'Super babouche', 100);