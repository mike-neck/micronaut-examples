create table if not exists authors(
    id bigint not null primary key ,
    first_name varchar(50) not null ,
    last_name varchar(50) not null
);

create table if not exists books(
    id bigint not null primary key ,
    name varchar(120) not null ,
    price int not null ,
    publication_date datetime not null
);

create table if not exists writings(
    book_id bigint not null ,
    author_id bigint not null ,
    constraint pk_writings primary key (book_id, author_id),
    constraint fk_writings_books foreign key (book_id) references books (id),
    constraint fk_writings_authors foreign key (author_id) references authors(id)
);
