curl -i -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"hello"}'
  

# Start ollama
ollama serve
  
  
# Start Qdrant
docker run -p 6333:6333 -p 6334:6334 -v $(pwd)/qdrant-data:/qdrant/storage qdrant/qdrant:v1.14.1

docker run --name qdrant \
  -p 6333:6333 -p 6334:6334 \
  -v $(pwd)/qdrant-data:/qdrant/data \
  qdrant/qdrant:v1.14.1
  
docker stop <container_id>
docker rm <container_id>