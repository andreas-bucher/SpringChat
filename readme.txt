curl -i -X POST http://localhost:8080/chat \
  -H "Content-Type: application/json" \
  -d '{"message":"hello"}'
  

# Start ollama
ollama serve
  
  
# Start Qdrant
cd /Users/ante
./qdrant

--> do not want to use Docker
docker run -p 6333:6333 -p 6334:6334 -v $(pwd)/qdrant-data:/qdrant/storage qdrant/qdrant:v1.14.1
docker run --name qdrant \
  -p 6333:6333 -p 6334:6334 \
  -v $(pwd)/qdrant-data:/qdrant/data \
  qdrant/qdrant:v1.14.1
docker stop <container_id>
docker rm <container_id>



curl -v http://localhost:8080/X2 \
  -H "Content-Type: application/json" \
  --data '{"collection":"springchat_tools_bge_m3","query":"Superminds","topK":5,"similarityThreshold":-1,"includeEmbeddings":false,"filters":null}'