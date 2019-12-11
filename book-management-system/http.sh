#!/usr/bin/env bash

echo "curl -i http://localhost:8080/authors/1"
curl -i http://localhost:8080/authors/1
echo ""
echo ""

echo "curl -i http://localhost:8080/authors -X POST -d 'firstName=三成' -d 'lastName=石田'"
curl -i http://localhost:8080/authors -X POST -d 'firstName=三成' -d 'lastName=石田'
echo ""
echo ""

echo "curl -i http://localhost:8080/authors/1"
curl -i http://localhost:8080/authors/1
echo ""
echo ""

echo "curl -i http://localhost:8080/authors/1/books -X POST -d 'name=罪と罰' -d 'price=3200' -d 'publish=2019-12-11T12:34:56.789Z'"
curl -i http://localhost:8080/authors/1/books -X POST -d 'name=罪と罰' -d 'price=3200' -d 'publish=2019-12-11T12:34:56.789Z'
echo ""
echo ""

echo "curl -i http://localhost:8080/authors/1/books/2"
curl -i http://localhost:8080/authors/1/books/2
echo ""
echo ""

echo "curl -i http://localhost:8080/books/2"
curl -i http://localhost:8080/books/2
echo ""
echo ""

echo "curl -i http://localhost:8080/books/2 -X PATCH -H 'content-type:application/json' -d '{\"name\":\"罪と罰 2nd edition\"}'"
curl -i http://localhost:8080/books/2 -X PATCH -H 'content-type:application/json' -d '{"name":"罪と罰 2nd edition"}'
echo ""
echo ""
