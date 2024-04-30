insert into category (id, description) values (1000, 'Comic Books');
insert into category (id, description) values (1001, 'Movies');
insert into category (id, description) values (1002, 'Books');

insert into supplier (id, name) values (1000, 'Panini Comics');
insert into supplier (id, name) values (1001, 'Amazon');

insert into product (id, name, fk_supplier, fk_category, quantity_available, created_at) values (1001, 'Crise nas Infinitas Terras', 1000, 1000, 10, current_timestamp);
insert into product (id, name, fk_supplier, fk_category, quantity_available, created_at) values (1002, 'Interestelar', 1001, 1001, 5, current_timestamp);
insert into product (id, name, fk_supplier, fk_category, quantity_available, created_at) values (1003, 'Harry Potter e a Pedra Filosofal', 1001, 1002, 3, current_timestamp);